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
package com.chensoul.system.domain.user.controller;

import static com.chensoul.system.ControllerConstants.USER_ID;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.setting.domain.SecuritySetting;
import com.chensoul.system.domain.usage.limit.LimitedApi;
import com.chensoul.system.infrastructure.common.BaseController;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import com.chensoul.system.infrastructure.security.util.SecurityUtils;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getCurrentUser;
import com.chensoul.system.user.domain.User;
import com.chensoul.system.user.domain.UserCredential;
import com.chensoul.system.user.model.PasswordChangeRequest;
import com.chensoul.system.user.model.PasswordResetEmailRequest;
import com.chensoul.system.user.model.PasswordResetRequest;
import com.chensoul.system.user.model.UserActivateRequest;
import com.chensoul.util.RestResponse;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    @GetMapping(value = "/auth/user")
    public RestResponse<User> getUser() {
        return RestResponse.ok(checkUserId(SecurityUtils.getUserId()));
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    @PostMapping(value = "/auth/logout")
    public RestResponse logout() {
        auditLogService.doAndLog(getCurrentUser(), EntityType.USER, ActionType.LOGOUT, t -> userService.logout());
        return RestResponse.ok();
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PostMapping(value = "/auth/sendActivationMail")
    public RestResponse sendActivationEmail(@RequestParam(value = "email") String email, HttpServletRequest request) {
        auditLogService.doAndLog(getCurrentUser(), EntityType.USER, ActionType.CREDENTIAL_UPDATE, t -> authService.sendActivationEmail(email, request));
        return RestResponse.ok();
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/auth/{userId}/activationLink", produces = "text/plain")
    public String getActivationLink(@PathVariable(USER_ID) Long userId, HttpServletRequest request) {
        return authService.getActivationLink(userId, request);
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    @PostMapping(value = "/auth/changePassword")
    public RestResponse<JwtPair> changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest) {
        return RestResponse.ok((JwtPair) auditLogService.doAndLog(getCurrentUser(), null, EntityType.USER, ActionType.CREDENTIAL_UPDATE, t -> userService.changePassword(passwordChangeRequest)));
    }

    @GetMapping(value = "/noauth/userPasswordPolicy")
    public RestResponse<SecuritySetting.PasswordPolicy> getUserPasswordPolicy() {
        return RestResponse.ok(systemSettingService.getSecuritySetting().getPasswordPolicy());
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
    public RestResponse<Void> requestResetPasswordByEmail(@RequestBody PasswordResetEmailRequest resetPasswordByEmailRequest) {
        auditLogService.doAndLog(resetPasswordByEmailRequest, EntityType.USER, ActionType.CREDENTIAL_UPDATE,
            t -> userService.requestResetPasswordByEmail(resetPasswordByEmailRequest.getEmail()));
        return RestResponse.ok();
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
        //跳转到重置密码页面
        return new ResponseEntity<>(headers, responseStatus);
    }

    @PostMapping(value = "/noauth/activate")
    public RestResponse<JwtPair> activateUser(@RequestBody UserActivateRequest activateRequest,
                                              @RequestParam(required = false, defaultValue = "true") boolean sendActivationMail) {
        return RestResponse.ok((JwtPair) auditLogService.doAndLog(activateRequest, null, EntityType.USER, ActionType.CREDENTIAL_UPDATE,
            t -> userService.activateUser(activateRequest, sendActivationMail)));
    }

    @PostMapping(value = "/noauth/resetPassword")
    public RestResponse<JwtPair> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest) {
        return RestResponse.ok((JwtPair) auditLogService.doAndLog(passwordResetRequest, null, EntityType.USER, ActionType.CREDENTIAL_UPDATE,
            t -> userService.resetPassword(passwordResetRequest)));
    }
}
