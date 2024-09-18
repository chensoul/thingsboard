package com.chensoul.system.domain.user.service.impl;

import static com.chensoul.data.validation.Validators.checkNotNull;
import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.notification.channel.mail.MailService;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.domain.user.service.AuthService;
import com.chensoul.system.domain.user.service.UserService;
import com.chensoul.system.user.domain.User;
import com.chensoul.system.user.domain.UserCredential;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final SystemSettingService systemSettingService;
    private final MailService mailService;

    @Override
    public void sendActivationEmail(String email, HttpServletRequest request) {
        User user = userService.findUserByEmail(email);
        UserCredential userCredential = checkNotNull(userService.findUserCredentialByUserId(user.getId()));
        if (!userCredential.isEnabled() && userCredential.getActivateToken() != null) {
            String baseUrl = systemSettingService.getBaseUrl(request);
            String activateUrl = String.format(ACTIVATE_URL_PATTERN, baseUrl, userCredential.getActivateToken());
            mailService.sendActivationEmail(activateUrl, user.getEmail());
        } else {
            throw new BusinessException("User is already activated!");
        }
    }

    @Override
    public String getActivationLink(Long userId, HttpServletRequest request) {
        UserCredential userCredential = checkNotNull(userService.findUserCredentialByUserId(userId));
        if (!userCredential.isEnabled() && userCredential.getActivateToken() != null) {
            String baseUrl = systemSettingService.getBaseUrl(request);
            return String.format(ACTIVATE_URL_PATTERN, baseUrl, userCredential.getActivateToken());
        } else {
            throw new BusinessException("User is already activated!");
        }
    }
}
