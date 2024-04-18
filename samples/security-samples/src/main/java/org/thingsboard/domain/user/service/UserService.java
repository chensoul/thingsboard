/**
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
package org.thingsboard.domain.user.service;

import java.util.Set;
import org.springframework.data.domain.Page;
import org.thingsboard.common.dao.EntityDaoService;
import org.thingsboard.common.dao.jpa.PageData;
import org.thingsboard.common.dao.jpa.PageLink;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.server.security.SecurityUser;

public interface UserService extends EntityDaoService<Long> {

	User findUserById(Long id);

	User findUserByEmail(String email);

	User saveUser(User user, boolean sendActivationMail) throws ThingsboardException;

	User saveUser(User user);

	void deleteUser(User user);

	UserCredential findUserCredentialByUserId(Long id);

	UserCredential saveUserCredential(UserCredential userCredential);

	UserCredential requestExpiredPasswordReset(Long id);

	UserCredential replaceUserCredential(UserCredential userCredential);

	UserCredential findUserCredentialByActivateToken(String activateToken);

	UserCredential findUserCredentialByResetToken(String resetToken);

	UserCredential requestPasswordReset(String email);

	UserCredential activateUserCredential(String activateToken, String encodedPassword);

	void setUserCredentialsEnabled(Long userId, boolean userCredentialsEnabled);

	PageData<User> findUsers(PageLink pageLink);

	PageData<User> findUsersByIds(Set<Long> ids, PageLink pageLink);

	PageData<User> findUsersByTenantIdsAndAuthority(Set<String> tenantIds, Authority authority, PageLink pageLink);

	PageData<User> findUsersByAuthority(Authority authority, PageLink pageLink);

	PageData<User> findUsersByTenantId(String tenantId, PageLink pageLink);

	PageData<User> findUsersByMerchantIds(Set<Long> merchantIds, PageLink pageLink);

	void resetFailedLoginAttempt(Long userId);

	int increaseFailedLoginAttempt(Long userId);

	void setLastLoginTs(Long id);

	SecurityUser changePassword(String currentPassword, String newPassword) throws ThingsboardException;
}
