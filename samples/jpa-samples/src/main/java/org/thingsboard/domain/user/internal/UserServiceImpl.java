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
package org.thingsboard.domain.user.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import static org.thingsboard.common.exception.ErrorExceptionHandler.YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasId;
import org.thingsboard.common.model.event.ActionEntityEvent;
import org.thingsboard.common.model.event.DeleteEntityEvent;
import org.thingsboard.common.model.event.SaveEntityEvent;
import org.thingsboard.common.util.JacksonUtil;
import static org.thingsboard.common.validation.Validator.validateString;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.notification.internal.channel.mail.MailService;
import org.thingsboard.domain.setting.SecuritySettingService;
import org.thingsboard.domain.user.UserCredentialInvalidationEvent;
import org.thingsboard.domain.user.Authority;
import org.thingsboard.domain.user.User;
import org.thingsboard.domain.user.UserCredential;
import org.thingsboard.domain.user.internal.persistence.UserCredentialDao;
import org.thingsboard.domain.user.internal.persistence.UserDao;
import org.thingsboard.domain.user.internal.persistence.UserSettingDao;
import org.thingsboard.domain.user.UserService;
import static org.thingsboard.domain.user.internal.AuthServiceImpl.ACTIVATE_URL_PATTERN;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.UserPrincipal;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.server.security.jwt.token.JwtPair;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private static final int DEFAULT_TOKEN_LENGTH = 30;
	public static final String USER_CREDENTIALS_ENABLED = "userCredentialsEnabled";
	public static final String LAST_LOGIN_TS = "lastLoginTs";
	public static final String FAILED_LOGIN_ATTEMPTS = "failedLoginAttempts";
	public static final String USER_PASSWORD_HISTORY = "userPasswordHistory";

	@Value("${security.user_login_case_sensitive:true}")
	private boolean userLoginCaseSensitive;

	@Value("${security.user_token_access_enabled}")
	private boolean userTokenAccessEnabled;

	private final JwtTokenFactory tokenFactory;
	private final ApplicationEventPublisher eventPublisher;
	private final MailService mailService;
	private final SecuritySettingService securitySettingService;
	private final HttpServletRequest request;
	private final PasswordEncoder passwordEncoder;
	private final UserValidator userValidator;
	private final UserCredentialValidator userCredentialValidator;
	private final UserCredentialDao userCredentialDao;
	private final UserDao userDao;
	private final UserSettingDao userSettingDao;

	@Override
	public User findUserById(Long id) {
		return userDao.findById(id);
	}

	@Override
	public User findUserByEmail(String email) {
		validateString(email, "Incorrect email " + email);

		if (userLoginCaseSensitive) {
			return userDao.findByEmail(email);
		} else {
			return userDao.findByEmail(email.toLowerCase());
		}
	}

	@Override
	public User saveUser(User user, boolean sendActivationMail) {
		User savedUser = saveUser(user);

		boolean sendEmail = user.getId() == null && sendActivationMail;
		if (sendEmail) {
			UserCredential userCredential = findUserCredentialByUserId(savedUser.getId());
			String baseUrl = securitySettingService.getBaseUrl(request);
			String activateUrl = String.format(ACTIVATE_URL_PATTERN, baseUrl,
				userCredential.getActivateToken());
			String email = savedUser.getEmail();
			try {
				mailService.sendActivationEmail(activateUrl, email);
			} catch (ThingsboardException e) {
				deleteUser(savedUser);
				throw e;
			}
		}

		return savedUser;
	}

	@Override
	public User saveUser(User user) {
		User oldUser = userValidator.validate(user);

		if (!userLoginCaseSensitive) {
			user.setEmail(user.getEmail().toLowerCase());
		}

		User savedUser = userDao.save(user);

		if (user.getId() == null) {
			UserCredential userCredential = new UserCredential();
			userCredential.setEnabled(false);
			userCredential.setActivateToken(generateSafeToken(DEFAULT_TOKEN_LENGTH));
			userCredential.setUserId(savedUser.getId());
			userCredential.setExtra(JacksonUtil.newObjectNode());
			userCredentialDao.save(userCredential);
		}
		eventPublisher.publishEvent(SaveEntityEvent.builder()
			.entity(savedUser)
			.oldEntity(oldUser)
			.created(user.getId() == null).build());
		return savedUser;
	}

	@Override
	public void deleteUser(User user) {
		userValidator.validateDelete(user);

		userCredentialDao.removeByUserId(user.getId());
		userSettingDao.removeByUserId(user.getId());
		userDao.removeById(user.getId());

		eventPublisher.publishEvent(new UserCredentialInvalidationEvent(user.getId()));
		eventPublisher.publishEvent(DeleteEntityEvent.builder().tenantId(user.getTenantId()).entity(user).build());
	}

	@Override
	public JwtPair getUserToken(User user) {
		if (!userTokenAccessEnabled) {
			throw new ThingsboardException(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION,
				ThingsboardErrorCode.PERMISSION_DENIED);
		}
		UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
		UserCredential credentials = findUserCredentialByUserId(user.getId());
		SecurityUser securityUser = new SecurityUser(user, credentials.isEnabled(), principal);
		return tokenFactory.createTokenPair(securityUser);
	}

	@Override
	public UserCredential findUserCredentialByUserId(Long userId) {
		return userCredentialDao.findByUserId((Long) userId);
	}

	@Override
	public UserCredential findUserCredentialByActivateToken(String activateToken) {
		return userCredentialDao.findByActivateToken(activateToken);
	}

	@Override
	public UserCredential findUserCredentialByResetToken(String resetToken) {
		return userCredentialDao.findByResetToken(resetToken);
	}

	@Override
	public UserCredential requestPasswordReset(String email) {
		User user = findUserByEmail(email);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("Unable to find user by email [%s]", email));
		}
		UserCredential userCredential = findUserCredentialByUserId(user.getId());
		if (!userCredential.isEnabled()) {
			throw new DisabledException(String.format("User credentials not enabled [%s]", email));
		}
		userCredential.setResetToken(generateSafeToken(DEFAULT_TOKEN_LENGTH));
		return saveUserCredential(userCredential);
	}

	@Override
	public UserCredential activateUserCredential(String activateToken, String encodedPassword) {
		UserCredential userCredential = findUserCredentialByActivateToken(activateToken);
		if (userCredential == null) {
			throw new DataValidationException(String.format("Unable to find user credentials by activateToken [%s]", activateToken));
		}
		if (userCredential.isEnabled()) {
			throw new DataValidationException("User credentials already activated");
		}
		userCredential.setEnabled(true);
		userCredential.setActivateToken(null);
		userCredential.setPassword(encodedPassword);
		if (userCredential.getPassword() != null) {
			updatePasswordHistory(userCredential);
		}
		return saveUserCredential(userCredential);
	}

	@Override
	public UserCredential saveUserCredential(UserCredential userCredential) {
		userCredentialValidator.validate(userCredential);

		UserCredential result = userCredentialDao.save(userCredential);

		eventPublisher.publishEvent(ActionEntityEvent.builder()
			.entityId(userCredential.getUserId())
			.actionType(ActionType.CREDENTIALS_UPDATE).build());

		return result;
	}

	@Override
	public void setUserCredentialsEnabled(Long userId, boolean userCredentialsEnabled) {
		UserCredential userCredential = findUserCredentialByUserId(userId);
		userCredential.setEnabled(userCredentialsEnabled);
		saveUserCredential(userCredential);

		User user = findUserById(userId);
		JsonNode extra = user.getExtra();
		if (!(extra instanceof ObjectNode)) {
			extra = JacksonUtil.newObjectNode();
		}
		((ObjectNode) extra).put(USER_CREDENTIALS_ENABLED, userCredentialsEnabled);
		user.setExtra(extra);

		if (userCredentialsEnabled) {
			resetFailedLoginAttempts(user);
		} else {
			eventPublisher.publishEvent(new UserCredentialInvalidationEvent((Long) userId));
		}
		saveUser(user);
	}

	public PageData<User> findUsersByMerchantIds(Set<Long> merchantIds, PageLink pageLink) {
		if (CollectionUtils.isEmpty(merchantIds)) {
			return PageData.empty();
		}
		return userDao.findByMerchantIds(merchantIds, pageLink);
	}

	@Override
	public PageData<User> findUsersByTenantId(String tenantId, PageLink pageLink) {
		return userDao.findByTenantId(tenantId, pageLink);
	}

	@Override
	public PageData<User> findUsersByIds(Set<Long> ids, PageLink pageLink) {
		if (CollectionUtils.isEmpty(ids)) {
			return PageData.empty();
		}
		return userDao.findUsersByIds(ids, pageLink);
	}

	@Override
	public PageData<User> findUsers(PageLink pageLink) {
		SecurityUser currentUser = getCurrentUser();
		if (Authority.SYS_ADMIN.equals(currentUser.getAuthority())) {
			return userDao.findUsers(pageLink);
		} else if (Authority.TENANT_ADMIN.equals(currentUser.getAuthority())) {
			return findUsersByTenantId(currentUser.getTenantId(), pageLink);
		} else {
			if (currentUser.getMerchantId() == null) {
				throw new ThingsboardException("merchantId is null", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
			}
			return findUsersByMerchantIds(Set.of(currentUser.getMerchantId()), pageLink);
		}
	}

	@Override
	public PageData<User> findUsersByTenantIdsAndAuthority(Set<String> tenantIds, Authority authority, PageLink pageLink) {
		if (CollectionUtils.isEmpty(tenantIds)) {
			return PageData.empty();
		}
		return userDao.findByTenantIdsAndAuthority(tenantIds, authority, pageLink);
	}

	@Override
	public PageData<User> findUsersByAuthority(Authority authority, PageLink pageLink) {
		return userDao.findByAuthority(authority, pageLink);
	}

	@Override
	public void resetFailedLoginAttempt(Long userId) {
		User user = findUserById(userId);
		resetFailedLoginAttempts(user);
		saveUser(user);
	}

	@Override
	public int increaseFailedLoginAttempt(Long userId) {
		User user = findUserById(userId);
		int failedLoginAttempts = increaseFailedLoginAttempts(user);
		saveUser(user);
		return failedLoginAttempts;
	}

	private int increaseFailedLoginAttempts(User user) {
		JsonNode additionalInfo = user.getExtra();
		if (!(additionalInfo instanceof ObjectNode)) {
			additionalInfo = JacksonUtil.newObjectNode();
		}
		int failedLoginAttempts = 0;
		if (additionalInfo.has(FAILED_LOGIN_ATTEMPTS)) {
			failedLoginAttempts = additionalInfo.get(FAILED_LOGIN_ATTEMPTS).asInt();
		}
		failedLoginAttempts = failedLoginAttempts + 1;
		((ObjectNode) additionalInfo).put(FAILED_LOGIN_ATTEMPTS, failedLoginAttempts);
		user.setExtra(additionalInfo);
		return failedLoginAttempts;
	}

	@Override
	public UserCredential requestExpiredPasswordReset(Long id) {
		UserCredential userCredential = findUserCredentialByUserId(id);
		if (!userCredential.isEnabled()) {
			throw new DataValidationException("Unable to reset password for inactive user");
		}
		userCredential.setResetToken(generateSafeToken(DEFAULT_TOKEN_LENGTH));
		return saveUserCredential(userCredential);
	}

	@Override
	public UserCredential replaceUserCredential(UserCredential userCredential) {
		userCredentialValidator.validate(userCredential);
		userCredentialDao.removeById(userCredential.getId());
		userCredential.setId(null);
		if (userCredential.getPassword() != null) {
			updatePasswordHistory(userCredential);
		}
		UserCredential result = saveUserCredential(userCredential);
		eventPublisher.publishEvent(ActionEntityEvent.builder()
			.entityId(userCredential.getUserId())
			.actionType(ActionType.CREDENTIALS_UPDATE).build());
		return result;
	}

	private void updatePasswordHistory(UserCredential userCredential) {
		JsonNode additionalInfo = userCredential.getExtra();
		if (!(additionalInfo instanceof ObjectNode)) {
			additionalInfo = JacksonUtil.newObjectNode();
		}
		Map<String, String> userPasswordHistoryMap = null;
		JsonNode userPasswordHistoryJson;
		if (additionalInfo.has(USER_PASSWORD_HISTORY)) {
			userPasswordHistoryJson = additionalInfo.get(USER_PASSWORD_HISTORY);
			userPasswordHistoryMap = JacksonUtil.convertValue(userPasswordHistoryJson, new TypeReference<>() {
			});
		} else {
			userPasswordHistoryMap = new HashMap<>();
		}
		userPasswordHistoryMap.put(Long.toString(System.currentTimeMillis()), userCredential.getPassword());

		userPasswordHistoryMap = keepFirstNRecords(userPasswordHistoryMap, 3);
		userPasswordHistoryJson = JacksonUtil.valueToTree(userPasswordHistoryMap);
		((ObjectNode) additionalInfo).set(USER_PASSWORD_HISTORY, userPasswordHistoryJson);
		userCredential.setExtra(additionalInfo);
	}

	private static <K, V> Map<K, V> keepFirstNRecords(Map<K, V> map, int n) {
		return map.entrySet()
			.stream()
			.limit(n)
			.collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
	}

	private String generateSafeToken(int defaultTokenLength) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(RandomStringUtils.randomAlphabetic(defaultTokenLength).getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void setLastLoginTs(Long id) {
		User user = findUserById(id);
		JsonNode additionalInfo = user.getExtra();
		if (!(additionalInfo instanceof ObjectNode)) {
			additionalInfo = JacksonUtil.newObjectNode();
		}
		((ObjectNode) additionalInfo).put(LAST_LOGIN_TS, System.currentTimeMillis());
		user.setExtra(additionalInfo);
		saveUser(user);
	}

	@Override
	public SecurityUser changePassword(String currentPassword, String newPassword) {
		SecurityUser securityUser = getCurrentUser();
		UserCredential userCredential = findUserCredentialByUserId(securityUser.getId());
		if (!passwordEncoder.matches(currentPassword, userCredential.getPassword())) {
			throw new ThingsboardException("Current password doesn't match!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
		securitySettingService.validatePassword(newPassword, userCredential);
		if (passwordEncoder.matches(newPassword, userCredential.getPassword())) {
			throw new ThingsboardException("New password should be different from existing!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
		userCredential.setPassword(passwordEncoder.encode(newPassword));
		replaceUserCredential(userCredential);

		eventPublisher.publishEvent(new UserCredentialInvalidationEvent(securityUser.getId()));
		return securityUser;
	}

	private void resetFailedLoginAttempts(User user) {
		JsonNode extra = user.getExtra();
		if (!(extra instanceof ObjectNode)) {
			extra = JacksonUtil.newObjectNode();
		}
		((ObjectNode) extra).put(FAILED_LOGIN_ATTEMPTS, 0);
		user.setExtra(extra);
	}

	@Override
	public Optional<HasId<Long>> findEntity(Long id) {
		return Optional.ofNullable(findUserById(id));
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.USER;
	}

	@Override
	public long countByTenantId(String tenantId) {
		return userDao.countByTenantId(tenantId);
	}
}
