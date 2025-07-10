/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.setting.internal; /**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static org.thingsboard.common.CacheConstants.SECURITY_SETTING_CACHE;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.MiscUtils;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.audit.AuditLogService;
import org.thingsboard.domain.notification.internal.channel.mail.MailService;
import org.thingsboard.domain.setting.TwoFaSystemSetting;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.SystemSettingService;
import org.thingsboard.domain.setting.SystemSettingType;
import org.thingsboard.domain.setting.PasswordPolicy;
import org.thingsboard.domain.setting.SecuritySetting;
import org.thingsboard.domain.setting.SecuritySettingService;
import org.thingsboard.domain.user.User;
import org.thingsboard.domain.user.UserCredential;
import org.thingsboard.domain.user.UserService;
import org.thingsboard.domain.user.internal.UserServiceImpl;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;
import org.thingsboard.server.security.rest.RestAuthenticationDetail;
import org.thingsboard.server.security.rest.exception.UserPasswordExpiredException;
import ua_parser.Client;

@Service
@Slf4j
public class DefaultSecuritySettingService implements SecuritySettingService {

	@Autowired
	private SystemSettingService systemSettingService;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	@Lazy
	private UserService userService;

	@Autowired
	private MailService mailService;

	@Resource
	private SecuritySettingService self;

	@Autowired
	private AuditLogService auditLogService;

	@Cacheable(cacheNames = SECURITY_SETTING_CACHE, key = "'securitySetting'")
	@Override
	public SecuritySetting getSecuritySetting() {
		SecuritySetting securitySetting = null;
		SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.SECURITY);
		if (systemSetting != null) {
			try {
				securitySetting = JacksonUtil.convertValue(systemSetting.getExtra(), SecuritySetting.class);
			} catch (Exception e) {
				throw new RuntimeException("Failed to load security setting", e);
			}
		} else {
			securitySetting = new SecuritySetting();
			securitySetting.setPasswordPolicy(new PasswordPolicy());
			securitySetting.getPasswordPolicy().setMinimumLength(6);
			securitySetting.getPasswordPolicy().setMaximumLength(72);
		}
		return securitySetting;
	}

	@CacheEvict(cacheNames = SECURITY_SETTING_CACHE, key = "'securitySetting'")
	@Override
	public SecuritySetting saveSecuritySetting(SecuritySetting securitySetting) {
		SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.SECURITY);
		if (systemSetting == null) {
			systemSetting = new SystemSetting();
			systemSetting.setTenantId(SYS_TENANT_ID);
			systemSetting.setType(SystemSettingType.SECURITY);
		}
		systemSetting.setExtra(JacksonUtil.valueToTree(securitySetting));
		SystemSetting savedSystemSetting = systemSettingService.saveSystemSetting(SYS_TENANT_ID, systemSetting);
		try {
			return JacksonUtil.convertValue(savedSystemSetting.getExtra(), SecuritySetting.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load security setting", e);
		}
	}

	@Override
	public void validateUserCredential(String tenantId, UserCredential userCredential, String username, String password) throws AuthenticationException {
		if (!encoder.matches(password, userCredential.getPassword())) {
			int failedLoginAttempts = userService.increaseFailedLoginAttempt(userCredential.getUserId());
			SecuritySetting securitySetting = self.getSecuritySetting();
			if (securitySetting.getMaxFailedLoginAttempts() != null && securitySetting.getMaxFailedLoginAttempts() > 0) {
				if (failedLoginAttempts > securitySetting.getMaxFailedLoginAttempts() && userCredential.isEnabled()) {
					lockAccount(userCredential.getUserId(), username, securitySetting.getUserLockoutNotificationEmail(), securitySetting.getMaxFailedLoginAttempts());
					throw new LockedException("User was locked due to security policy");
				}
			}
			throw new BadCredentialsException("Username or password not valid");
		}

		if (!userCredential.isEnabled()) {
			throw new DisabledException("User is not active");
		}

		userService.resetFailedLoginAttempt(userCredential.getUserId());

		SecuritySetting securitySetting = self.getSecuritySetting();
		if (isPositiveInteger(securitySetting.getPasswordPolicy().getPasswordExpirationPeriodDays()) && userCredential.getCreatedTime() != 0) {
			if ((userCredential.getCreatedTime()
				 + TimeUnit.DAYS.toMillis(securitySetting.getPasswordPolicy().getPasswordExpirationPeriodDays()))
				< System.currentTimeMillis()) {
				userCredential = userService.requestExpiredPasswordReset(userCredential.getId());
				throw new UserPasswordExpiredException("User password has expired", userCredential.getResetToken());
			}
		}
	}

	@Override
	public void validateTwoFaVerification(SecurityUser securityUser, boolean verificationSuccess, TwoFaSystemSetting twoFaSystemSettings) {
		Long userId = securityUser.getId();

		int failedVerificationAttempts;
		if (!verificationSuccess) {
			failedVerificationAttempts = userService.increaseFailedLoginAttempt(userId);
		} else {
			userService.resetFailedLoginAttempt(userId);
			return;
		}

		Integer maxVerificationFailures = twoFaSystemSettings.getMaxVerificationFailuresBeforeUserLockout();
		if (maxVerificationFailures != null && maxVerificationFailures > 0
			&& failedVerificationAttempts >= maxVerificationFailures) {
			userService.setUserCredentialsEnabled(userId, false);
			SecuritySetting securitySetting = self.getSecuritySetting();
			lockAccount(userId, securityUser.getEmail(), securitySetting.getUserLockoutNotificationEmail(), maxVerificationFailures);
			throw new LockedException("User was locked due to exceeded 2FA verification attempts");
		}
	}

	private void lockAccount(Long userId, String username, String userLockoutNotificationEmail, Integer maxFailedLoginAttempts) {
		userService.setUserCredentialsEnabled(userId, false);
		if (StringUtils.isNotBlank(userLockoutNotificationEmail)) {
			try {
				mailService.sendAccountLockoutEmail(username, userLockoutNotificationEmail, maxFailedLoginAttempts);
			} catch (ThingsboardException e) {
				log.warn("Can't send email regarding user account [{}] lockout to provided email [{}]", username, userLockoutNotificationEmail, e);
			}
		}
	}

	@Override
	public void validatePassword(String password, UserCredential userCredential) throws DataValidationException {
		SecuritySetting securitySetting = self.getSecuritySetting();
		PasswordPolicy passwordPolicy = securitySetting.getPasswordPolicy();

		validatePasswordByPolicy(password, passwordPolicy);

		if (userCredential != null && isPositiveInteger(passwordPolicy.getPasswordReuseFrequencyDays())) {
			long passwordReuseFrequencyTs = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(passwordPolicy.getPasswordReuseFrequencyDays());
			JsonNode additionalInfo = userCredential.getExtra();
			if (additionalInfo instanceof ObjectNode && additionalInfo.has(UserServiceImpl.USER_PASSWORD_HISTORY)) {
				JsonNode userPasswordHistoryJson = additionalInfo.get(UserServiceImpl.USER_PASSWORD_HISTORY);
				Map<String, String> userPasswordHistoryMap = JacksonUtil.convertValue(userPasswordHistoryJson, new TypeReference<>() {
				});
				for (Map.Entry<String, String> entry : userPasswordHistoryMap.entrySet()) {
					if (encoder.matches(password, entry.getValue()) && Long.parseLong(entry.getKey()) > passwordReuseFrequencyTs) {
						throw new DataValidationException("Password was already used for the last " + passwordPolicy.getPasswordReuseFrequencyDays() + " days");
					}
				}
			}
		}
	}

	@Override
	public void validatePasswordByPolicy(String password, PasswordPolicy passwordPolicy) {
		List<Rule> passwordRules = new ArrayList<>();

		Integer maximumLength = passwordPolicy.getMaximumLength();
		Integer minLengthBound = passwordPolicy.getMinimumLength();
		int maxLengthBound = (maximumLength != null && maximumLength > passwordPolicy.getMinimumLength()) ? maximumLength : Integer.MAX_VALUE;

		passwordRules.add(new LengthRule(minLengthBound, maxLengthBound));
		if (isPositiveInteger(passwordPolicy.getMinimumUppercaseLetters())) {
			passwordRules.add(new CharacterRule(EnglishCharacterData.UpperCase, passwordPolicy.getMinimumUppercaseLetters()));
		}
		if (isPositiveInteger(passwordPolicy.getMinimumLowercaseLetters())) {
			passwordRules.add(new CharacterRule(EnglishCharacterData.LowerCase, passwordPolicy.getMinimumLowercaseLetters()));
		}
		if (isPositiveInteger(passwordPolicy.getMinimumDigits())) {
			passwordRules.add(new CharacterRule(EnglishCharacterData.Digit, passwordPolicy.getMinimumDigits()));
		}
		if (isPositiveInteger(passwordPolicy.getMinimumSpecialCharacters())) {
			passwordRules.add(new CharacterRule(EnglishCharacterData.Special, passwordPolicy.getMinimumSpecialCharacters()));
		}
		if (passwordPolicy.getAllowWhitespaces() != null && !passwordPolicy.getAllowWhitespaces()) {
			passwordRules.add(new WhitespaceRule());
		}
		PasswordValidator validator = new PasswordValidator(passwordRules);
		PasswordData passwordData = new PasswordData(password);
		RuleResult result = validator.validate(passwordData);
		if (!result.isValid()) {
			String message = String.join("\n", validator.getMessages(result));
			throw new DataValidationException(message);
		}
	}

	@Override
	public String getBaseUrl(HttpServletRequest httpServletRequest) {
		String baseUrl = null;
		SystemSetting generalSettings = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.GENERAL);

		if (generalSettings != null) {
			JsonNode prohibitDifferentUrl = generalSettings.getExtra().get("prohibitDifferentUrl");

			if ((prohibitDifferentUrl != null && prohibitDifferentUrl.asBoolean())) {
				baseUrl = generalSettings.getExtra().get("baseUrl").asText();
			}
		}

		if (StringUtils.isEmpty(baseUrl) && httpServletRequest != null) {
			baseUrl = MiscUtils.constructBaseUrl(httpServletRequest);
		}

		return baseUrl;
	}

	@Override
	public void logLoginAction(User user, ActionType actionType, Exception e, Object authenticationDetails, String authProviderId) {
		String clientAddress = "Unknown";
		String serverAddress = "Unknown";
		String browser = "Unknown";
		String os = "Unknown";
		String device = "Unknown";
		if (authenticationDetails instanceof RestAuthenticationDetail) {
			RestAuthenticationDetail details = (RestAuthenticationDetail) authenticationDetails;
			clientAddress = details.getClientAddress();
			serverAddress = details.getServerAddress();
			if (details.getUserAgent() != null) {
				Client userAgent = details.getUserAgent();
				if (userAgent.userAgent != null) {
					browser = userAgent.userAgent.family;
					if (userAgent.userAgent.major != null) {
						browser += " " + userAgent.userAgent.major;
						if (userAgent.userAgent.minor != null) {
							browser += "." + userAgent.userAgent.minor;
							if (userAgent.userAgent.patch != null) {
								browser += "." + userAgent.userAgent.patch;
							}
						}
					}
				}
				if (userAgent.os != null) {
					os = userAgent.os.family;
					if (userAgent.os.major != null) {
						os += " " + userAgent.os.major;
						if (userAgent.os.minor != null) {
							os += "." + userAgent.os.minor;
							if (userAgent.os.patch != null) {
								os += "." + userAgent.os.patch;
								if (userAgent.os.patchMinor != null) {
									os += "." + userAgent.os.patchMinor;
								}
							}
						}
					}
				}
				if (userAgent.device != null) {
					device = userAgent.device.family;
				}
			}
		}
		if (actionType == ActionType.LOGIN && e == null) {
			userService.setLastLoginTs(user.getId());
		}
		ObjectNode actionData = JacksonUtil.newObjectNode();
		actionData.put("clientAddress", clientAddress);
		actionData.put("serverAddress", serverAddress);
		actionData.put("browser", browser);
		actionData.put("os", os);
		actionData.put("device", device);
		actionData.put("authProviderId", authProviderId);

		auditLogService.logEntityAction(user, user, EntityType.USER, actionType, e, actionData);
	}

	private static boolean isPositiveInteger(Integer val) {
		return val != null && val.intValue() > 0;
	}
}
