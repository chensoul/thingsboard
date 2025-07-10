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
package com.chensoul.system.domain.setting.service.impl;

import static com.chensoul.data.validation.Validators.isPositiveInteger;
import com.chensoul.exception.BusinessException;
import com.chensoul.json.JacksonUtils;
import com.chensoul.system.domain.notification.channel.mail.MailService;
import com.chensoul.system.domain.setting.domain.MfaSetting;
import com.chensoul.system.domain.setting.domain.SecuritySetting;
import com.chensoul.system.domain.setting.service.SecuritySettingService;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.domain.user.service.UserService;
import com.chensoul.system.domain.user.service.impl.UserServiceImpl;
import com.chensoul.system.infrastructure.security.rest.exception.UserPasswordExpiredException;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.UserCredential;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecuritySettingServiceImpl implements SecuritySettingService {

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Override
    public void validateUserCredential(String tenantId, UserCredential userCredential, String username, String password) throws AuthenticationException {
        SecuritySetting securitySetting = systemSettingService.getSecuritySetting();

        if (!encoder.matches(password, userCredential.getPassword())) {
            int failedLoginAttempts = userService.increaseFailedLoginAttempt(userCredential.getUserId());
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

        if (isPositiveInteger(securitySetting.getPasswordPolicy().getPasswordExpirationPeriodDays()) && userCredential.getCreateTime() != null) {
            if ((userCredential.getCreateTime().getNano()
                 + TimeUnit.DAYS.toMillis(securitySetting.getPasswordPolicy().getPasswordExpirationPeriodDays()))
                < System.currentTimeMillis()) {
                userCredential = userService.requestExpiredPasswordReset(userCredential.getId());
                throw new UserPasswordExpiredException("User password has expired", userCredential.getResetToken());
            }
        }
    }

    @Override
    public void validateMfaVerification(SecurityUser securityUser, boolean verificationSuccess, MfaSetting twoFaSystemSettings) {
        Long userId = securityUser.getId();

        int failedVerificationAttempts;
        if (!verificationSuccess) {
            failedVerificationAttempts = userService.increaseFailedLoginAttempt(userId);
        } else {
            userService.resetFailedLoginAttempt(userId);
            return;
        }

        SecuritySetting securitySetting = systemSettingService.getSecuritySetting();

        Integer maxVerificationFailures = twoFaSystemSettings.getMaxVerificationFailuresBeforeUserLockout();
        if (maxVerificationFailures != null && maxVerificationFailures > 0
            && failedVerificationAttempts >= maxVerificationFailures) {
            userService.setUserCredentialEnabled(userId, false);
            lockAccount(userId, securityUser.getEmail(), securitySetting.getUserLockoutNotificationEmail(), maxVerificationFailures);
            throw new LockedException("User was locked due to exceeded 2FA verification attempts");
        }
    }

    private void lockAccount(Long userId, String username, String userLockoutNotificationEmail, Integer maxFailedLoginAttempts) {
        userService.setUserCredentialEnabled(userId, false);
        if (StringUtils.isNotBlank(userLockoutNotificationEmail)) {
            try {
                mailService.sendAccountLockoutEmail(username, userLockoutNotificationEmail, maxFailedLoginAttempts);
            } catch (BusinessException e) {
                log.warn("Can't send email regarding user account [{}] lockout to provided email [{}]", username, userLockoutNotificationEmail, e);
            }
        }
    }

    @Override
    public void validatePassword(String password, UserCredential userCredential) {
        SecuritySetting securitySetting = systemSettingService.getSecuritySetting();
        SecuritySetting.PasswordPolicy passwordPolicy = securitySetting.getPasswordPolicy();

        validatePasswordByPolicy(password, passwordPolicy);

        if (userCredential != null && isPositiveInteger(passwordPolicy.getPasswordReuseFrequencyDays())) {
            long passwordReuseFrequencyTs = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(passwordPolicy.getPasswordReuseFrequencyDays());
            JsonNode additionalInfo = userCredential.getExtra();
            if (additionalInfo instanceof ObjectNode && additionalInfo.has(UserServiceImpl.USER_PASSWORD_HISTORY)) {
                JsonNode userPasswordHistoryJson = additionalInfo.get(UserServiceImpl.USER_PASSWORD_HISTORY);
                Map<String, String> userPasswordHistoryMap = JacksonUtils.convertValue(userPasswordHistoryJson, new TypeReference<Map<String, String>>() {
                });
                for (Map.Entry<String, String> entry : userPasswordHistoryMap.entrySet()) {
                    if (encoder.matches(password, entry.getValue()) && Long.parseLong(entry.getKey()) > passwordReuseFrequencyTs) {
                        throw new BusinessException("Password was already used for the last " + passwordPolicy.getPasswordReuseFrequencyDays() + " days");
                    }
                }
            }
        }
    }

    @Override
    public void validatePasswordByPolicy(String password, SecuritySetting.PasswordPolicy passwordPolicy) {
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
            throw new BusinessException(message);
        }
    }
}
