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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static org.thingsboard.common.ControllerConstants.USER_ID;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.BaseController;
import static org.thingsboard.common.validation.Validator.checkNotNull;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.usage.limit.LimitedApi;
import org.thingsboard.domain.notification.internal.channel.mail.MailService;
import org.thingsboard.domain.setting.PasswordPolicy;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.SecurityUtils;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.UserPrincipal;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.server.security.permission.Operation;
import org.thingsboard.server.security.permission.Resource;
import org.thingsboard.server.security.rest.RestAuthenticationDetail;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class AuthController extends BaseController {

	@Value("${server.rest.rate_limits.reset_password_per_user:5:3600}")
	private String defaultLimitsConfiguration;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenFactory tokenFactory;
	private final MailService mailService;
	private final ApplicationEventPublisher eventPublisher;
	private final AuthService authService;

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@GetMapping(value = "/auth/user")
	public User getUser() {
		return userService.findUserById(SecurityUtils.getUserId());
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PostMapping(value = "/auth/logout")
	public void logout(HttpServletRequest request) {
		logLogoutAction(request);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@PostMapping(value = "/auth/sendActivationMail")
	public void sendActivationEmail(@RequestParam(value = "email") String email,
									HttpServletRequest request) {
		User user = checkNotNull(userService.findUserByEmail(email));
		accessControlService.checkPermission(getCurrentUser(), Resource.USER, Operation.READ, user.getId(), user);

		authService.sendActivationEmail(email, request);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@GetMapping(value = "/auth/{userId}/activationLink", produces = "text/plain")
	public String getActivationLink(@PathVariable(USER_ID) Long userId,
									HttpServletRequest request) {
		User user = checkUserId(userId, Operation.READ);
		return authService.getActivationLink(user.getId(), request);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PostMapping(value = "/auth/changePassword")
	public JwtPair changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest) {
		String currentPassword = passwordChangeRequest.getCurrentPassword();
		String newPassword = passwordChangeRequest.getNewPassword();
		return tokenFactory.createTokenPair(userService.changePassword(currentPassword, newPassword));
	}

	@GetMapping(value = "/noauth/userPasswordPolicy")
	public PasswordPolicy getUserPasswordPolicy() {
		return securitySettingService.getSecuritySetting().getPasswordPolicy();
	}

	@GetMapping(value = "/noauth/activate")
	public ResponseEntity<String> checkActivateToken(@RequestParam(value = "activateToken") String activateToken) {
		HttpHeaders headers = new HttpHeaders();
		HttpStatus responseStatus;
		UserCredential userCredential = userService.findUserCredentialByActivateToken(activateToken);
		if (userCredential != null) {
			String createURI = "/login/createPassword";
			try {
				URI location = new URI(createURI + "?activateToken=" + activateToken);
				headers.setLocation(location);
				responseStatus = HttpStatus.SEE_OTHER;
			} catch (URISyntaxException e) {
				log.error("Unable to create URI with address [{}]", createURI);
				responseStatus = HttpStatus.BAD_REQUEST;
			}
		} else {
			responseStatus = HttpStatus.CONFLICT;
		}
		return new ResponseEntity<>(headers, responseStatus);
	}

	@PostMapping(value = "/noauth/resetPasswordByEmail")
	public void requestResetPasswordByEmail(@RequestBody PasswordResetEmailRequest resetPasswordByEmailRequest,
											HttpServletRequest request) {
		try {
			String email = resetPasswordByEmailRequest.getEmail();
			UserCredential userCredential = userService.requestPasswordReset(email);
			String baseUrl = securitySettingService.getBaseUrl(request);
			String resetUrl = String.format("%s/api/noauth/resetPassword?resetToken=%s", baseUrl,
				userCredential.getResetToken());

			mailService.sendResetPasswordEmailAsync(resetUrl, email);
		} catch (Exception e) {
			log.warn("Error occurred: {}", e.getMessage());
		}
	}

	@GetMapping(value = "/noauth/resetPassword")
	public ResponseEntity<String> checkResetToken(@RequestParam(value = "resetToken") String resetToken) {
		HttpHeaders headers = new HttpHeaders();
		HttpStatus responseStatus;
		String resetURI = "/login/resetPassword";
		UserCredential userCredential = userService.findUserCredentialByResetToken(resetToken);

		if (userCredential != null) {
			if (rateLimitService.checkRateLimited(LimitedApi.PASSWORD_RESET, userCredential.getUserId(), defaultLimitsConfiguration)) {
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
			}
			try {
				URI location = new URI(resetURI + "?resetToken=" + resetToken);
				headers.setLocation(location);
				responseStatus = HttpStatus.SEE_OTHER;
			} catch (URISyntaxException e) {
				log.error("Unable to create URI with address [{}]", resetURI);
				responseStatus = HttpStatus.BAD_REQUEST;
			}
		} else {
			responseStatus = HttpStatus.CONFLICT;
		}
		return new ResponseEntity<>(headers, responseStatus);
	}

	@PostMapping(value = "/noauth/activate")
	public JwtPair activateUser(@RequestBody UserActivateRequest activateRequest,
								@RequestParam(required = false, defaultValue = "true") boolean sendActivationMail,
								HttpServletRequest request) {
		String activateToken = activateRequest.getActivateToken();
		String password = activateRequest.getPassword();
		securitySettingService.validatePassword(password, null);
		String encodedPassword = passwordEncoder.encode(password);
		UserCredential credentials = userService.activateUserCredential(activateToken, encodedPassword);
		User user = userService.findUserById(credentials.getUserId());
		UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
		SecurityUser securityUser = new SecurityUser(user, credentials.isEnabled(), principal);
		userService.setUserCredentialsEnabled(user.getId(), true);
		String baseUrl = securitySettingService.getBaseUrl(request);
		String loginUrl = String.format("%s/login", baseUrl);
		String email = user.getEmail();

		if (sendActivationMail) {
			try {
				mailService.sendAccountActivatedEmail(loginUrl, email);
			} catch (Exception e) {
				log.info("Unable to send account activation email [{}]", e.getMessage());
			}
		}

		return tokenFactory.createTokenPair(securityUser);
	}

	@PostMapping(value = "/noauth/resetPassword")
	public JwtPair resetPassword(@RequestBody PasswordResetRequest passwordResetRequest,
								 HttpServletRequest request) {
		String resetToken = passwordResetRequest.getResetToken();
		String password = passwordResetRequest.getPassword();
		UserCredential userCredential = userService.findUserCredentialByResetToken(resetToken);
		if (userCredential != null) {
			securitySettingService.validatePassword(password, userCredential);
			if (passwordEncoder.matches(password, userCredential.getPassword())) {
				throw new ThingsboardException("New password should be different from existing!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
			}
			String encodedPassword = passwordEncoder.encode(password);
			userCredential.setPassword(encodedPassword);
			userCredential.setResetToken(null);
			userCredential = userService.replaceUserCredential(userCredential);
			User user = userService.findUserById(userCredential.getUserId());
			UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
			SecurityUser securityUser = new SecurityUser(user, userCredential.isEnabled(), principal);
			String baseUrl = securitySettingService.getBaseUrl(request);
			String loginUrl = String.format("%s/login", baseUrl);
			String email = user.getEmail();
			mailService.sendPasswordWasResetEmail(loginUrl, email);

			eventPublisher.publishEvent(new UserCredentialInvalidationEvent(securityUser.getId()));

			return tokenFactory.createTokenPair(securityUser);
		} else {
			throw new ThingsboardException("Invalid reset token!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
	}

	private void logLogoutAction(HttpServletRequest request) {
		SecurityUser securityUser = getCurrentUser();
		securitySettingService.logLoginAction(securityUser, ActionType.LOGOUT, null, new RestAuthenticationDetail(request), null);
		eventPublisher.publishEvent(new UserSessionInvalidationEvent(securityUser.getSessionId()));
	}
}
