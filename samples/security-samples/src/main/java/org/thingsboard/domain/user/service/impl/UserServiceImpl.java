package org.thingsboard.domain.user.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.event.ActionEntityEvent;
import org.thingsboard.common.model.event.DeleteEntityEvent;
import org.thingsboard.common.model.event.SaveEntityEvent;
import org.thingsboard.common.exception.DataValidationException;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasId;
import org.thingsboard.common.util.JacksonUtil;
import static org.thingsboard.common.validation.Validator.validateString;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.notification.channel.mail.MailService;
import org.thingsboard.domain.setting.security.SecuritySettingService;
import static org.thingsboard.domain.user.UserController.ACTIVATE_URL_PATTERN;
import org.thingsboard.domain.user.event.UserCredentialInvalidationEvent;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.domain.user.persistence.UserCredentialDao;
import org.thingsboard.domain.user.persistence.UserDao;
import org.thingsboard.domain.user.persistence.UserSettingDao;
import org.thingsboard.domain.user.service.UserCredentialValidator;
import org.thingsboard.domain.user.service.UserService;
import org.thingsboard.domain.user.service.UserValidator;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;

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
	public User findUserById(Serializable id) {
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
	public UserCredential findUserCredentialByUserId(Serializable userId) {
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
	public void setUserCredentialsEnabled(Serializable userId, boolean userCredentialsEnabled) {
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

	public Page<User> findMerchantUsers(Pageable pageable, Set<Long> merchantIds, String textSearch) {
		return userDao.findTenantAndCustomerUsers(pageable, null, merchantIds, null, null, textSearch);
	}

	@Override
	public Page<User> findTenantUsers(Pageable pageable, String tenantId, String textSearch) {
		return userDao.findTenantAndCustomerUsers(pageable, Set.of(tenantId), null, null, null, textSearch);
	}

	@Override
	public Page<User> findUsers(Pageable pageable, Set<Long> ids, String textSearch) {
		return userDao.findTenantAndCustomerUsers(pageable, null, null, ids, null, textSearch);
	}

	@Override
	public Page<User> findTenantAdminsByTenantsIds(Pageable pageable, Set<String> tenantIds) {
		return userDao.findTenantAndCustomerUsers(pageable, tenantIds, null, null, Authority.TENANT_ADMIN, null);
	}

	@Override
	public Page<User> findAllTenantAdmins(Pageable pageable) {
		return userDao.findTenantAndCustomerUsers(pageable, null, null, null, Authority.TENANT_ADMIN, null);
	}

	@Override
	public Page<User> findAllSysAdmins(Pageable pageable) {
		return userDao.findTenantAndCustomerUsers(pageable, null, null, null, Authority.SYS_ADMIN, null);
	}

	@Override
	public void resetFailedLoginAttempt(Serializable userId) {
		User user = findUserById(userId);
		resetFailedLoginAttempts(user);
		saveUser(user);
	}

	@Override
	public int increaseFailedLoginAttempt(Serializable userId) {
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
	public UserCredential requestExpiredPasswordReset(Serializable id) {
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
	public void setLastLoginTs(Serializable id) {
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
	public Optional<HasId<? extends Serializable>> findEntity(Serializable id) {
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
