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
package org.thingsboard.domain.user;

import java.util.Set;
import org.thingsboard.data.dao.EntityDaoService;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.jwt.token.JwtPair;

public interface UserService extends EntityDaoService<Long> {

	User findUserById(Long id);

	User findUserByEmail(String email);

	User saveUser(User user, boolean sendActivationMail);

	User saveUser(User user);

	void deleteUser(User user);

	JwtPair getUserToken(User user);

	UserCredential findUserCredentialByUserId(Long id);

	UserCredential saveUserCredential(UserCredential userCredential);

	UserCredential requestExpiredPasswordReset(Long id);

	UserCredential replaceUserCredential(UserCredential userCredential);

	UserCredential findUserCredentialByActivateToken(String activateToken);

	UserCredential findUserCredentialByResetToken(String resetToken);

	UserCredential requestPasswordReset(String email);

	UserCredential activateUserCredential(String activateToken, String encodedPassword);

	PageData<User> findUsers(PageLink pageLink);

	PageData<User> findUsersByIds(Set<Long> ids, PageLink pageLink);

	PageData<User> findUsersByTenantIdsAndAuthority(Set<String> tenantIds, Authority authority, PageLink pageLink);

	PageData<User> findUsersByAuthority(Authority authority, PageLink pageLink);

	PageData<User> findUsersByTenantId(String tenantId, PageLink pageLink);

	PageData<User> findUsersByMerchantIds(Set<Long> merchantIds, PageLink pageLink);

	void resetFailedLoginAttempt(Long userId);

	int increaseFailedLoginAttempt(Long userId);

	void setUserCredentialsEnabled(Long userId, boolean userCredentialsEnabled);

	void setLastLoginTs(Long id);

	SecurityUser changePassword(String currentPassword, String newPassword);
}
