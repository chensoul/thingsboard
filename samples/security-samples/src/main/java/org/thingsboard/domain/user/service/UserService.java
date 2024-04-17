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

import java.io.Serializable;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thingsboard.common.dao.EntityDaoService;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.server.security.SecurityUser;

public interface UserService extends EntityDaoService {

	User findUserById(Serializable id);

	User findUserByEmail(String email);

	User saveUser(User user, boolean sendActivationMail) throws ThingsboardException;

	User saveUser(User user);

	void deleteUser(User user);

	UserCredential findUserCredentialByUserId(Serializable id);

	UserCredential saveUserCredential(UserCredential userCredential);

	UserCredential requestExpiredPasswordReset(Serializable id);

	UserCredential replaceUserCredential(UserCredential userCredential);

	UserCredential findUserCredentialByActivateToken(String activateToken);

	UserCredential findUserCredentialByResetToken(String resetToken);

	UserCredential requestPasswordReset(String email);

	UserCredential activateUserCredential(String activateToken, String encodedPassword);

	void setUserCredentialsEnabled(Serializable userId, boolean userCredentialsEnabled);

	Page<User> findUsers(Pageable pageable, Set<Long> ids, String textSearch);

	Page<User> findTenantAdminsByTenantsIds(Pageable pageable, Set<String> tenantIds);

	Page<User> findTenantUsers(Pageable pageable, String tenantId, String textSearch);

	Page<User> findMerchantUsers(Pageable pageable, Set<Long> merchantIds, String textSearch);

	Page<User> findAllTenantAdmins(Pageable pageable);

	Page<User> findAllSysAdmins(Pageable pageable);

	void resetFailedLoginAttempt(Serializable userId);

	int increaseFailedLoginAttempt(Serializable userId);

	void setLastLoginTs(Serializable id);

	SecurityUser changePassword(String currentPassword, String newPassword) throws ThingsboardException;
}
