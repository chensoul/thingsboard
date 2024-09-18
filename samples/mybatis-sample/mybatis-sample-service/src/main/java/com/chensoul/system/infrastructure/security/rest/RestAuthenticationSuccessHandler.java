package com.chensoul.system.infrastructure.security.rest;

import com.chensoul.json.JacksonUtils;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.audit.service.AuditLogService;
import com.chensoul.system.domain.setting.service.MfaSettingService;
import com.chensoul.system.infrastructure.security.jwt.JwtTokenFactory;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import com.chensoul.system.infrastructure.security.jwt.token.TwoFaAuthenticationToken;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.Authority;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(value = "defaultAuthenticationSuccessHandler")
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenFactory tokenFactory;
    private final MfaSettingService mfaSettingService;
    private final AuditLogService auditLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        JwtPair tokenPair;
        if (authentication instanceof TwoFaAuthenticationToken) {
            int preVerificationTokenLifetime = mfaSettingService.getSystemMfaSetting(true)
                .flatMap(setting -> Optional.ofNullable(setting.getTotalAllowedTimeForVerification())
                    .filter(time -> time > 0))
                .orElse((int) TimeUnit.MINUTES.toSeconds(30));
            tokenPair = new JwtPair();
            tokenPair.setToken(tokenFactory.createPreVerificationToken(securityUser, preVerificationTokenLifetime).getToken());
            tokenPair.setScope(Authority.PRE_VERIFICATION_TOKEN);
        } else {
            tokenPair = tokenFactory.createTokenPair(securityUser);
        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        JacksonUtils.writeValue(response.getWriter(), tokenPair);

        auditLogService.logEntityAction(securityUser, securityUser, EntityType.USER, ActionType.LOGIN, null, null);

        clearAuthenticationAttributes(request);
    }

    /**
     * Removes temporary authentication-related data which may have been stored
     * in the session during the authentication process..
     */
    protected final void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
