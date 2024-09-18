package com.chensoul.system.infrastructure.security.rest;

import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.audit.service.AuditLogService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(value = "defaultAuthenticationFailureHandler")
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ErrorExceptionHandler errorResponseHandler;
    private final AuditLogService auditLogService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException e) throws IOException, ServletException {
        errorResponseHandler.handle(e, response);

        ActionType actionType = e instanceof LockedException ? ActionType.LOCKOUT : ActionType.LOGIN;
        auditLogService.logEntityAction(null, null, EntityType.USER, actionType, e, null);
    }
}
