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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import static org.thingsboard.common.validation.Validator.checkNotNull;
import org.thingsboard.domain.notification.internal.channel.mail.MailService;
import org.thingsboard.domain.setting.SecuritySettingService;
import org.thingsboard.domain.user.User;
import org.thingsboard.domain.user.UserCredential;
import org.thingsboard.domain.user.AuthService;
import org.thingsboard.domain.user.UserService;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
	public static final String ACTIVATE_URL_PATTERN = "%s/api/noauth/activate?activateToken=%s";

	private final UserService userService;
	private final SecuritySettingService securitySettingService;
	private final MailService mailService;


	@Override
	public void sendActivationEmail(String email, HttpServletRequest request) {
		User user = checkNotNull(userService.findUserByEmail(email));
		UserCredential userCredential = userService.findUserCredentialByUserId(user.getId());
		if (!userCredential.isEnabled() && userCredential.getActivateToken() != null) {
			String baseUrl = securitySettingService.getBaseUrl(request);
			String activateUrl = String.format(ACTIVATE_URL_PATTERN, baseUrl, userCredential.getActivateToken());
			mailService.sendActivationEmail(activateUrl, email);
		} else {
			throw new ThingsboardException("User is already activated!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
	}

	@Override
	public String getActivationLink(Long userId, HttpServletRequest request) {
		UserCredential userCredential = userService.findUserCredentialByUserId(userId);
		if (!userCredential.isEnabled() && userCredential.getActivateToken() != null) {
			String baseUrl = securitySettingService.getBaseUrl(request);
			return String.format(ACTIVATE_URL_PATTERN, baseUrl, userCredential.getActivateToken());
		} else {
			throw new ThingsboardException("User is already activated!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
	}
}
