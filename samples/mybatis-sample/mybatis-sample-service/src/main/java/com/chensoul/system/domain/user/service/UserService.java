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
package com.chensoul.system.domain.user.service;

import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.system.infrastructure.common.EntityDaoService;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import com.chensoul.system.user.domain.Authority;
import com.chensoul.system.user.domain.User;
import com.chensoul.system.user.domain.UserCredential;
import com.chensoul.system.user.model.PasswordChangeRequest;
import com.chensoul.system.user.model.PasswordResetRequest;
import com.chensoul.system.user.model.UserActivateRequest;
import java.util.List;
import java.util.Set;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface UserService extends EntityDaoService {
    User findUserById(Long id);

    User findUserByEmail(String email);

    User saveUser(User user, boolean sendActivationMail);

    User saveUser(User user);

    void deleteUser(User user);

    List<User> findUsers();

    PageData<User> findUsers(PageLink pageLink);

    PageData<User> findUsersByIds(Set<Long> ids, PageLink pageLink);

    PageData<User> findUsersByTenantIdsAndAuthority(Set<String> tenantIds, Authority authority, PageLink pageLink);

    PageData<User> findUsersByAuthority(Authority authority, PageLink pageLink);

    PageData<User> findUsersByTenantId(String tenantId, PageLink pageLink);

    PageData<User> findUsersByMerchantIds(Set<Long> merchantIds, PageLink pageLink);

    JwtPair getUserToken(Long userId);

    JwtPair activateUser(UserActivateRequest activateRequest, boolean sendActivationMail);

    JwtPair resetPassword(PasswordResetRequest passwordResetRequest);

    JwtPair changePassword(PasswordChangeRequest passwordChangeRequest);

    void logout();

    UserCredential activateUserCredential(String activateToken, String encodedPassword);

    UserCredential findUserCredentialByUserId(Long userId);

    UserCredential saveUserCredential(UserCredential userCredential);

    UserCredential requestExpiredPasswordReset(Long userId);

    UserCredential replaceUserCredential(UserCredential userCredential);

    UserCredential findUserCredentialByActivateToken(String activateToken);

    UserCredential findUserCredentialByResetToken(String resetToken);

    void requestResetPasswordByEmail(String email);

    void resetFailedLoginAttempt(Long userId);

    int increaseFailedLoginAttempt(Long userId);

    User setUserCredentialEnabled(Long userId, boolean userCredentialsEnabled);

    void setLastLoginTime(Long userId);

}
