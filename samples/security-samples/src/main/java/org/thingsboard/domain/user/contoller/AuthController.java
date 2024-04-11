package org.thingsboard.domain.user.contoller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.limit.LimitedApi;
import org.thingsboard.domain.limit.RateLimitService;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.domain.user.service.UserService;
import org.thingsboard.server.mail.MailService;
import org.thingsboard.server.security.SecurityUtils;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.rest.RestAuthenticationDetails;
import org.thingsboard.domain.user.event.UserCredentialInvalidationEvent;
import org.thingsboard.domain.user.event.UserSessionInvalidationEvent;
import org.thingsboard.domain.user.model.UserActivateRequest;
import org.thingsboard.domain.user.model.PasswordChangeRequest;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.domain.user.model.PasswordResetEmailRequest;
import org.thingsboard.domain.user.model.PasswordResetRequest;
import org.thingsboard.domain.setting.security.SecuritySetting;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.domain.setting.security.PasswordPolicy;
import org.thingsboard.server.security.UserPrincipal;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.domain.setting.security.SecuritySettingService;

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
public class AuthController {
	@Value("${server.rest.rate_limits.reset_password_per_user:5:3600}")
	private String defaultLimitsConfiguration;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenFactory tokenFactory;
	private final MailService mailService;
	private final UserService userService;
	private final SecuritySettingService securitySettingService;
	private final RateLimitService rateLimitService;
	private final ApplicationEventPublisher eventPublisher;

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@GetMapping(value = "/auth/user")
	public User getUser() {
		return userService.findUserById(SecurityUtils.getUserId());
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PostMapping(value = "/auth/logout")
	@ResponseStatus(value = HttpStatus.OK)
	public void logout(HttpServletRequest request) {
		logLogoutAction(request);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	@PostMapping(value = "/auth/changePassword")
	@ResponseStatus(value = HttpStatus.OK)
	public JwtPair changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest) {
		String currentPassword = passwordChangeRequest.getCurrentPassword();
		String newPassword = passwordChangeRequest.getNewPassword();
		return tokenFactory.createTokenPair(userService.changePassword(currentPassword, newPassword));
	}

	@GetMapping(value = "/noauth/userPasswordPolicy")
	public PasswordPolicy getUserPasswordPolicy() {
		SecuritySetting securitySetting = securitySettingService.getSecuritySettings();
		return securitySetting.getPasswordPolicy();
	}

	@GetMapping(value = "/noauth/activate")
	public ResponseEntity<String> checkActivateToken(@RequestParam(value = "activateToken") String activateToken) {
		HttpHeaders headers = new HttpHeaders();
		HttpStatus responseStatus;
		UserCredential userCredential = userService.findUserCredentialsByActivateToken(activateToken);
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
	@ResponseStatus(value = HttpStatus.OK)
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
		UserCredential userCredential = userService.findUserCredentialsByResetToken(resetToken);

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
	@ResponseStatus(value = HttpStatus.OK)
	public JwtPair activateUser(@RequestBody UserActivateRequest activateRequest,
								@RequestParam(required = false, defaultValue = "true") boolean sendActivationMail,
								HttpServletRequest request) {
		String activateToken = activateRequest.getActivateToken();
		String password = activateRequest.getPassword();
		securitySettingService.validatePassword(password, null);
		String encodedPassword = passwordEncoder.encode(password);
		UserCredential credentials = userService.activateUserCredentials(activateToken, encodedPassword);
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
	@ResponseStatus(value = HttpStatus.OK)
	public JwtPair resetPassword(@RequestBody PasswordResetRequest passwordResetRequest,
								 HttpServletRequest request) {
		String resetToken = passwordResetRequest.getResetToken();
		String password = passwordResetRequest.getPassword();
		UserCredential userCredential = userService.findUserCredentialsByResetToken(resetToken);
		if (userCredential != null) {
			securitySettingService.validatePassword(password, userCredential);
			if (passwordEncoder.matches(password, userCredential.getPassword())) {
				throw new ThingsboardException("New password should be different from existing!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
			}
			String encodedPassword = passwordEncoder.encode(password);
			userCredential.setPassword(encodedPassword);
			userCredential.setResetToken(null);
			userCredential = userService.replaceUserCredentials(userCredential);
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
		securitySettingService.logLoginAction(securityUser, ActionType.LOGOUT, null, new RestAuthenticationDetails(request), null);
		eventPublisher.publishEvent(new UserSessionInvalidationEvent(securityUser.getSessionId()));
	}
}
