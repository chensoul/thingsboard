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
package com.chensoul.system.infrastructure.security.rest;

import com.chensoul.exception.BusinessException;
import com.chensoul.exception.ResultCode;
import static com.chensoul.exception.ResultCode.TOO_MANY_REQUESTS;
import com.chensoul.json.JacksonUtils;
import com.chensoul.system.domain.usage.limit.RateLimitedException;
import com.chensoul.system.infrastructure.security.jwt.JwtExpiredTokenException;
import com.chensoul.system.infrastructure.security.rest.exception.AuthMethodNotSupportedException;
import com.chensoul.system.infrastructure.security.rest.exception.CredentialsExpiredResponse;
import com.chensoul.system.infrastructure.security.rest.exception.UserPasswordExpiredException;
import com.chensoul.system.infrastructure.security.rest.exception.UserPasswordNotValidException;
import com.chensoul.util.ErrorResponse;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ErrorExceptionHandler implements AccessDeniedHandler {
    public static final String YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION = "You don't have permission to perform this operation!";
    private final HttpServletRequest request;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        if (response.isCommitted()) {
            return;
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.FORBIDDEN.getCode(), YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION));

        logException(request, response, exception);
    }

    public void handle(Exception exception, HttpServletResponse response) {
        if (response.isCommitted()) {
            return;
        }

        try {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            if (exception instanceof BusinessException) {

            } else if (exception instanceof DataAccessException) {
                handleSqlException((DataAccessException) exception, response);
            } else if (exception instanceof RateLimitedException) {
                handleRateLimitException(response, (RateLimitedException) exception);
            } else if (exception instanceof AccessDeniedException) {
                handleAccessDeniedException(response);
            } else if (exception instanceof AuthenticationException) {
                handleAuthenticationException((AuthenticationException) exception, response);
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.INTERNAL_ERROR.getCode(), ResultCode.INTERNAL_ERROR.getName()));
            }

            logException(request, response, exception);
        } catch (IOException e) {
            log.error("Can't handle exception", e);
        }
    }

    private void handleRateLimitException(HttpServletResponse response, RateLimitedException exception) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        String message = "Too many requests for current " + exception.getEntityType().name().toLowerCase() + "!";
        JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(TOO_MANY_REQUESTS.getCode(), message));
    }

    private void handleAccessDeniedException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.FORBIDDEN.getCode(), YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION));
    }

    private void handleSqlException(DataAccessException e, HttpServletResponse response) throws IOException {
        String message = "database error";
        String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
        if (rootCauseMessage != null && rootCauseMessage.contains("Duplicate entry")) {
            final String[] split = rootCauseMessage.split(" ");
            message = split[2] + " already exists";
        }

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.BAD_REQUEST.getCode(), message));
    }

    private void handleAuthenticationException(AuthenticationException authenticationException, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        if (authenticationException instanceof BadCredentialsException || authenticationException instanceof UsernameNotFoundException) {
            JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.BAD_REQUEST.getCode(), "Invalid username or password"));
        } else if (authenticationException instanceof DisabledException) {
            JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.BAD_REQUEST.getCode(), "User is not active"));
        } else if (authenticationException instanceof LockedException) {
            JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.BAD_REQUEST.getCode(), "User is locked due to security policy"));
        } else if (authenticationException instanceof JwtExpiredTokenException) {
            JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.BAD_REQUEST.getCode(), authenticationException.getMessage()));
        } else if (authenticationException instanceof AuthMethodNotSupportedException || authenticationException instanceof AuthenticationServiceException) {
            JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.BAD_REQUEST.getCode(), authenticationException.getMessage()));
        } else if (authenticationException instanceof UserPasswordExpiredException) {
            UserPasswordExpiredException expiredException = (UserPasswordExpiredException) authenticationException;
            String resetToken = expiredException.getResetToken();
            JacksonUtils.writeValue(response.getWriter(), CredentialsExpiredResponse.of(expiredException.getMessage(), resetToken));
        } else if (authenticationException instanceof UserPasswordNotValidException) {
            UserPasswordNotValidException expiredException = (UserPasswordNotValidException) authenticationException;
            JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.UNAUTHORIZED.getCode(), expiredException.getMessage()));
        } else {
            JacksonUtils.writeValue(response.getWriter(), ErrorResponse.of(ResultCode.UNAUTHORIZED.getCode(), "Authentication failed"));
        }
    }

    private void logException(HttpServletRequest request, HttpServletResponse response, Throwable throwable) {
        log.error("Processing exception for {}, return http status: {}", request.getRequestURI(), response.getStatus(), throwable);
    }
}
