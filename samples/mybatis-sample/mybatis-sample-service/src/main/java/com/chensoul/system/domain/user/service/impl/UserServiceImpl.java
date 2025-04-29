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
package com.chensoul.system.domain.user.service.impl;

import com.chensoul.constant.DateConstants;
import com.chensoul.data.event.DeleteEntityEvent;
import com.chensoul.data.event.SaveEntityEvent;
import com.chensoul.data.model.HasId;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import static com.chensoul.data.validation.Validators.checkNotNull;
import com.chensoul.exception.BusinessException;
import com.chensoul.json.JacksonUtils;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.notification.channel.mail.MailService;
import com.chensoul.system.domain.setting.service.SecuritySettingService;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.domain.user.mybatis.UserCredentialDao;
import com.chensoul.system.domain.user.mybatis.UserDao;
import com.chensoul.system.domain.user.mybatis.UserSettingDao;
import com.chensoul.system.domain.user.service.UserCredentialValidator;
import com.chensoul.system.domain.user.service.UserService;
import com.chensoul.system.domain.user.service.UserValidator;
import com.chensoul.system.domain.user.service.event.UserCredentialInvalidationEvent;
import com.chensoul.system.domain.user.service.event.UserSessionInvalidationEvent;
import com.chensoul.system.infrastructure.security.jwt.JwtTokenFactory;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import static com.chensoul.system.infrastructure.security.rest.ErrorExceptionHandler.YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getCurrentUser;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getUserId;
import com.chensoul.system.infrastructure.security.util.UserPrincipal;
import com.chensoul.system.user.domain.Authority;
import com.chensoul.system.user.domain.User;
import com.chensoul.system.user.domain.UserCredential;
import com.chensoul.system.user.model.PasswordChangeRequest;
import com.chensoul.system.user.model.PasswordResetRequest;
import com.chensoul.system.user.model.UserActivateRequest;
import com.chensoul.util.date.DateTimeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    public static final String ACTIVATE_URL_PATTERN = "%s/api/noauth/activate?activateToken=%s";
    public static final String USER_CREDENTIAL_ENABLED = "userCredentialEnabled";
    public static final String LAST_LOGIN_TIME = "lastLoginTime";
    public static final String FAILED_LOGIN_ATTEMPTS = "failedLoginAttempts";
    public static final String USER_PASSWORD_HISTORY = "userPasswordHistory";
    private static final int DEFAULT_TOKEN_LENGTH = 30;

    private final UserDao userDao;
    private final UserCredentialDao userCredentialDao;
    private final UserSettingDao userSettingDao;
    private final UserValidator userValidator;
    private final UserCredentialValidator userCredentialValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final MailService mailService;
    private final HttpServletRequest request;
    private final SecuritySettingService securitySettingService;
    private final SystemSettingService systemSettingService;
    private final JwtTokenFactory tokenFactory;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.user_login_case_sensitive:true}")
    private boolean userLoginCaseSensitive;

    @Value("${security.user_token_access_enabled:true}")
    private boolean userTokenAccessEnabled;

    @Override
    public User findUserById(Long id) {
        User user = userDao.findById(id);

        if (user != null && user.getExtra().isObject() && !user.getExtra().has(USER_CREDENTIAL_ENABLED)) {
            ObjectNode extra = (ObjectNode) user.getExtra();
            UserCredential userCredential = findUserCredentialByUserId(user.getId());
            if (userCredential.isEnabled() && !extra.has(USER_CREDENTIAL_ENABLED)) {
                extra.put(USER_CREDENTIAL_ENABLED, true);
            }
        }
        return user;
    }

    @Override
    public User findUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User saveUser(User user) {
        User oldUser = userValidator.validate(user);

        if (!userLoginCaseSensitive) {
            user.setEmail(user.getEmail().toLowerCase());
        }

        if (user.getId() == null) {
            user.setExtra(JacksonUtils.newObjectNode().put(USER_CREDENTIAL_ENABLED, false));
        }
        User savedUser = userDao.save(user);

        if (user.getId() == null) {
            UserCredential userCredential = new UserCredential();
            userCredential.setEnabled(false);
            userCredential.setActivateToken(generateSafeToken(DEFAULT_TOKEN_LENGTH));
            userCredential.setUserId(savedUser.getId());
            userCredential.setExtra(JacksonUtils.newObjectNode());
            userCredentialDao.save(userCredential);
        }
        eventPublisher.publishEvent(SaveEntityEvent.builder()
            .entity(savedUser)
            .oldEntity(oldUser)
            .created(user.getId() == null).build());
        return savedUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User saveUser(User user, boolean sendActivationMail) {
        User savedUser = saveUser(user);

        boolean sendEmail = user.getId() == null && sendActivationMail;
        if (sendEmail) {
            UserCredential userCredential = findUserCredentialByUserId(savedUser.getId());
            String baseUrl = systemSettingService.getBaseUrl(request);
            String activateUrl = String.format(ACTIVATE_URL_PATTERN, baseUrl, userCredential.getActivateToken());
            String email = savedUser.getEmail();
            try {
                mailService.sendActivationEmail(activateUrl, email);
            } catch (BusinessException e) {
                deleteUser(savedUser);
                throw e;
            }
        }

        return savedUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(User user) {
        userValidator.validateDelete(user);

        userCredentialDao.removeByUserId(user.getId());
        userSettingDao.removeByUserId(user.getId());
        userDao.removeById(user.getId());

        eventPublisher.publishEvent(new UserCredentialInvalidationEvent(user.getId()));
        eventPublisher.publishEvent(DeleteEntityEvent.builder().tenantId(user.getTenantId()).entity(user).build());
    }

    @Override
    public List<User> findUsers() {
        return userDao.find();
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
                throw new BusinessException("merchantId is null");
            }
            return findUsersByMerchantIds(new HashSet<>(Arrays.asList(currentUser.getMerchantId())), pageLink);
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
    public JwtPair getUserToken(Long userId) {
        if (!userTokenAccessEnabled) {
            throw new BusinessException(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION);
        }
        User user = findUserById(userId);

        UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
        UserCredential credential = findUserCredentialByUserId(user.getId());
        SecurityUser securityUser = new SecurityUser(user, credential.isEnabled(), principal);
        return tokenFactory.createTokenPair(securityUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JwtPair activateUser(UserActivateRequest activateRequest, boolean sendActivationMail) {
        String password = activateRequest.getPassword();
        securitySettingService.validatePassword(password, null);
        String encodedPassword = passwordEncoder.encode(password);
        UserCredential credential = activateUserCredential(activateRequest.getActivateToken(), encodedPassword);

        User user = findUserById(credential.getUserId());
        setUserCredentialEnabled(user.getId(), true);

        if (sendActivationMail) {
            String baseUrl = systemSettingService.getBaseUrl(request);
            String loginUrl = String.format("%s/login", baseUrl);
            try {
                mailService.sendAccountActivatedEmail(loginUrl, user.getEmail());
            } catch (Exception e) {
                log.info("Unable to send account activation email [{}]", e.getMessage());
            }
        }

        UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
        SecurityUser securityUser = new SecurityUser(user, credential.isEnabled(), principal);
        return tokenFactory.createTokenPair(securityUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JwtPair resetPassword(PasswordResetRequest passwordResetRequest) {
        String resetToken = passwordResetRequest.getResetToken();
        UserCredential userCredential = findUserCredentialByResetToken(resetToken);
        if (userCredential == null) {
            throw new BusinessException("Invalid reset token!");
        }

        String password = passwordResetRequest.getPassword();
        securitySettingService.validatePassword(password, userCredential);
        if (passwordEncoder.matches(password, userCredential.getPassword())) {
            throw new BusinessException("New password should be different from existing!");
        }
        String encodedPassword = passwordEncoder.encode(password);
        userCredential.setPassword(encodedPassword);
        userCredential.setResetToken(null);
        userCredential = replaceUserCredential(userCredential);
        User user = findUserById(userCredential.getUserId());
        UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
        SecurityUser securityUser = new SecurityUser(user, userCredential.isEnabled(), principal);
        String baseUrl = systemSettingService.getBaseUrl(request);
        String loginUrl = String.format("%s/login", baseUrl);
        String email = user.getEmail();
        mailService.sendPasswordWasResetEmail(loginUrl, email);

        eventPublisher.publishEvent(new UserCredentialInvalidationEvent(securityUser.getId()));

        return tokenFactory.createTokenPair(securityUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JwtPair changePassword(PasswordChangeRequest passwordChangeRequest) {
        UserCredential userCredential = checkNotNull(findUserCredentialByUserId(getUserId()));

        String currentPassword = passwordChangeRequest.getCurrentPassword();
        String newPassword = passwordChangeRequest.getNewPassword();
        if (!passwordEncoder.matches(currentPassword, userCredential.getPassword())) {
            throw new BusinessException("Current password doesn't match!");
        }
        securitySettingService.validatePassword(newPassword, userCredential);
        if (passwordEncoder.matches(newPassword, userCredential.getPassword())) {
            throw new BusinessException("New password should be different from existing!");
        }
        userCredential.setPassword(passwordEncoder.encode(newPassword));
        replaceUserCredential(userCredential);

        eventPublisher.publishEvent(new UserCredentialInvalidationEvent(userCredential.getUserId()));

        return tokenFactory.createTokenPair(getCurrentUser());
    }

    @Override
    public void logout() {
        log.info("Logout action for user: [{}], from IP: [{}]", getCurrentUser(), request.getRemoteAddr());
        eventPublisher.publishEvent(new UserSessionInvalidationEvent(getCurrentUser().getSessionId()));
    }

    @Override
    public UserCredential activateUserCredential(String activateToken, String encodedPassword) {
        UserCredential userCredential = findUserCredentialByActivateToken(activateToken);
        if (userCredential == null) {
            throw new BusinessException(String.format("Unable to find user credentials by activateToken [%s]", activateToken));
        }
        if (userCredential.isEnabled()) {
            throw new BusinessException("User credentials already activated");
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
    public UserCredential findUserCredentialByUserId(Long userId) {
        return userCredentialDao.findByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredential saveUserCredential(UserCredential userCredential) {
        userCredentialValidator.validate(userCredential);

        return userCredentialDao.save(userCredential);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredential requestExpiredPasswordReset(Long id) {
        UserCredential userCredential = findUserCredentialByUserId(id);
        if (!userCredential.isEnabled()) {
            throw new BusinessException("Unable to reset password for inactive user");
        }
        userCredential.setResetToken(generateSafeToken(DEFAULT_TOKEN_LENGTH));
        return saveUserCredential(userCredential);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCredential replaceUserCredential(UserCredential userCredential) {
        if (userCredential.getPassword() != null) {
            updatePasswordHistory(userCredential);
        }
        return saveUserCredential(userCredential);
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
    @Transactional(rollbackFor = Exception.class)
    public void requestResetPasswordByEmail(String email) {
        User user = findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("邮箱[%s]不存在", email));
        }
        UserCredential userCredential = findUserCredentialByUserId(user.getId());
        if (userCredential == null) {
            throw new BusinessException(String.format("用户凭证[%s]不存在", email));
        }
        if (!userCredential.isEnabled()) {
            throw new BusinessException(String.format("用户凭证[%s]未激活", email));
        }
        userCredential.setResetToken(generateSafeToken(DEFAULT_TOKEN_LENGTH));
        saveUserCredential(userCredential);

        String baseUrl = systemSettingService.getBaseUrl(request);
        String resetUrl = String.format("%s/api/noauth/resetPassword?resetToken=%s", baseUrl,
            userCredential.getResetToken());
        mailService.sendResetPasswordEmailAsync(resetUrl, email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetFailedLoginAttempt(Long userId) {
        User user = findUserById(userId);
        resetFailedLoginAttempts(user);
        saveUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int increaseFailedLoginAttempt(Long userId) {
        User user = findUserById(userId);
        int failedLoginAttempts = increaseFailedLoginAttempts(user);
        saveUser(user);
        return failedLoginAttempts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User setUserCredentialEnabled(Long userId, boolean userCredentialEnabled) {
        UserCredential userCredential = findUserCredentialByUserId(userId);
        userCredential.setEnabled(userCredentialEnabled);
        saveUserCredential(userCredential);

        User user = findUserById(userId);
        JsonNode extra = user.getExtra();
        if (!(extra instanceof ObjectNode)) {
            extra = JacksonUtils.newObjectNode();
        }
        ((ObjectNode) extra).put(USER_CREDENTIAL_ENABLED, userCredentialEnabled);
        user.setExtra(extra);

        if (userCredentialEnabled) {
            resetFailedLoginAttempts(user);
        } else {
            eventPublisher.publishEvent(new UserCredentialInvalidationEvent((Long) userId));
        }
        return saveUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setLastLoginTime(Long userId) {
        User user = findUserById(userId);
        JsonNode extra = user.getExtra();
        if (!(extra instanceof ObjectNode)) {
            extra = JacksonUtils.newObjectNode();
        }
        ((ObjectNode) extra).put(LAST_LOGIN_TIME, DateTimeUtils.format(LocalDateTime.now(), DateConstants.NORM_DATETIME));
        user.setExtra(extra);
        saveUser(user);
    }

    @Override
    public Optional<HasId> findEntity(Serializable id) {
        return Optional.ofNullable(findUserById((Long) id));
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.USER;
    }

    private void resetFailedLoginAttempts(User user) {
        JsonNode extra = user.getExtra();
        if (!(extra instanceof ObjectNode)) {
            extra = JacksonUtils.newObjectNode();
        }
        ((ObjectNode) extra).put(FAILED_LOGIN_ATTEMPTS, 0);
        user.setExtra(extra);
    }

    private int increaseFailedLoginAttempts(User user) {
        JsonNode extra = user.getExtra();
        if (!(extra instanceof ObjectNode)) {
            extra = JacksonUtils.newObjectNode();
        }
        int failedLoginAttempts = 0;
        if (extra.has(FAILED_LOGIN_ATTEMPTS)) {
            failedLoginAttempts = extra.get(FAILED_LOGIN_ATTEMPTS).asInt();
        }
        failedLoginAttempts = failedLoginAttempts + 1;
        ((ObjectNode) extra).put(FAILED_LOGIN_ATTEMPTS, failedLoginAttempts);
        user.setExtra(extra);
        return failedLoginAttempts;
    }

    private void updatePasswordHistory(UserCredential userCredential) {
        JsonNode extra = userCredential.getExtra();
        if (!(extra instanceof ObjectNode)) {
            extra = JacksonUtils.newObjectNode();
        }
        Map<String, String> userPasswordHistoryMap = null;
        JsonNode history;
        if (extra.has(USER_PASSWORD_HISTORY)) {
            history = extra.get(USER_PASSWORD_HISTORY);
            userPasswordHistoryMap = JacksonUtils.convertValue(history, new TypeReference<Map<String, String>>() {
            });
        } else {
            userPasswordHistoryMap = new HashMap<>();
        }
        userPasswordHistoryMap.put(DateTimeUtils.format(LocalDateTime.now(), DateConstants.NORM_DATETIME), userCredential.getPassword());

        userPasswordHistoryMap = keepFirstNRecords(userPasswordHistoryMap, 3);
        history = JacksonUtils.valueToTree(userPasswordHistoryMap);
        ((ObjectNode) extra).set(USER_PASSWORD_HISTORY, history);
        userCredential.setExtra(extra);
    }

    private <K, V> Map<K, V> keepFirstNRecords(Map<K, V> map, int n) {
        return map.entrySet()
            .stream()
            .limit(n)
            .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }

    private String generateSafeToken(int defaultTokenLength) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(RandomStringUtils.randomAlphabetic(defaultTokenLength).getBytes(StandardCharsets.UTF_8));
    }
}
