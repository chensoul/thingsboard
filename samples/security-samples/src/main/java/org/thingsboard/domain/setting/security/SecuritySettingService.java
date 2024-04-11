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
package org.thingsboard.domain.setting.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.AuthenticationException;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.domain.setting.mfa.TwoFaSystemSetting;

public interface SecuritySettingService {

	SecuritySetting getSecuritySettings();

	SecuritySetting saveSecuritySettings(SecuritySetting securitySetting);

	void validatePasswordByPolicy(String password, PasswordPolicy passwordPolicy);

	void validateUserCredentials(String tenantId, UserCredential userCredential, String username, String password) throws AuthenticationException;

	void validateTwoFaVerification(SecurityUser securityUser, boolean verificationSuccess, TwoFaSystemSetting twoFaSystemSettings);

	void validatePassword(String password, UserCredential userCredential);

	String getBaseUrl(HttpServletRequest httpServletRequest);

	void logLoginAction(User user, ActionType actionType, Exception e, Object authenticationDetails, String provider);

}
