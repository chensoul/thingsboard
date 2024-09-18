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
package org.thingsboard.domain.user.internal.persistence;

import org.thingsboard.data.dao.Dao;
import org.thingsboard.domain.user.UserCredential;

/**
 * The Interface UserCredentialsDao.
 */
public interface UserCredentialDao extends Dao<UserCredential, Long> {

	/**
	 * Save or update user credentials object
	 *
	 * @param userCredential the user credentials object
	 * @return saved user credentials object
	 */
	UserCredential save(UserCredential userCredential);

	/**
	 * Find user credentials by user id.
	 *
	 * @param userId the user id
	 * @return the user credentials object
	 */
	UserCredential findByUserId(Long userId);

	/**
	 * Find user credentials by activate token.
	 *
	 * @param activateToken the activate token
	 * @return the user credentials object
	 */
	UserCredential findByActivateToken(String activateToken);

	/**
	 * Find user credentials by reset token.
	 *
	 * @param resetToken the reset token
	 * @return the user credentials object
	 */
	UserCredential findByResetToken(String resetToken);

	void removeByUserId(Long userId);

}
