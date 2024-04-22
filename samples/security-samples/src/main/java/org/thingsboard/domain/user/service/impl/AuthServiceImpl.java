package org.thingsboard.domain.user.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import static org.thingsboard.common.validation.Validator.checkNotNull;
import org.thingsboard.domain.notification.channel.mail.MailService;
import org.thingsboard.domain.setting.security.SecuritySettingService;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.model.UserCredential;
import org.thingsboard.domain.user.service.AuthService;
import org.thingsboard.domain.user.service.UserService;

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
