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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.data.service.DataValidator;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.domain.user.persistence.UserCredentialDao;

@Component
public class UserCredentialValidator extends DataValidator<UserCredential> {

	@Autowired
	private UserCredentialDao userCredentialDao;

	@Autowired
	@Lazy
	private UserService userService;

	@Override
	protected void validateCreate(UserCredential userCredential) {
		throw new DataValidationException("Creation of new user credentials is prohibited.");
	}

	@Override
	protected void validateDataImpl(UserCredential userCredential) {
		if (userCredential.getUserId() == null) {
			throw new DataValidationException("User credentials should be assigned to user!");
		}
		if (userCredential.isEnabled()) {
			if (StringUtils.isEmpty(userCredential.getPassword())) {
				throw new DataValidationException("Enabled user credentials should have password!");
			}
			if (StringUtils.isNotEmpty(userCredential.getActivateToken())) {
				throw new DataValidationException("Enabled user credentials can't have activate token!");
			}
		}
		UserCredential existingUserCredentialEntity = userCredentialDao.findById(userCredential.getId());
		if (existingUserCredentialEntity == null) {
			throw new DataValidationException("Unable to update non-existent user credentials!");
		}
		User user = userService.findUserById(userCredential.getUserId());
		if (user == null) {
			throw new DataValidationException("Can't assign user credentials to non-existent user!");
		}
	}
}
