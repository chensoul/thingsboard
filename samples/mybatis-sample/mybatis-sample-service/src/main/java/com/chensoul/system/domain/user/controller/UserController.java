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

import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import static com.chensoul.system.ControllerConstants.MERCHANT_ID;
import static com.chensoul.system.ControllerConstants.TENANT_ID;
import static com.chensoul.system.ControllerConstants.USER_ID;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.infrastructure.common.BaseController;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import com.chensoul.system.infrastructure.security.util.SecurityUtils;
import com.chensoul.system.user.domain.Authority;
import com.chensoul.system.user.domain.User;
import com.chensoul.util.RestResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Validated
@RequestMapping("/api/users")
@RestController
public class UserController extends BaseController {
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    @PostMapping
    public RestResponse<User> saveUser(@Valid @RequestBody User user, @RequestParam(required = false, defaultValue = "true") boolean sendActivationMail) throws Exception {
        if (!Authority.SYS_ADMIN.equals(SecurityUtils.getCurrentUser().getAuthority())) {
            user.setTenantId(SecurityUtils.getCurrentUser().getTenantId());
        }
        User old = userService.findUserById(user.getId());
        return RestResponse.ok((User) auditLogService.doAndLog(user, old, EntityType.USER, ActionType.ADD, t -> userService.saveUser(t, sendActivationMail)));
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @DeleteMapping(value = "/{userId}")
    public RestResponse<Void> deleteUser(@PathVariable(USER_ID) Long userId) throws Exception {
        User old = checkUserId(userId);
        auditLogService.doAndLog(old, EntityType.USER, ActionType.DELETE, t -> userService.deleteUser(old));
        return RestResponse.ok();
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PostMapping("/{userId}/userCredential/enabled")
    public RestResponse<User> setUserCredentialsEnabled(@PathVariable(USER_ID) Long userId,
                                                        @RequestParam(required = false, defaultValue = "true") boolean userCredentialEnabled) throws Exception {
        return RestResponse.ok((User) auditLogService.doAndLog(SecurityUtils.getCurrentUser(), null, EntityType.USER, ActionType.UPDATE, t -> userService.setUserCredentialEnabled(userId, userCredentialEnabled)));
    }

    @GetMapping("/list")
    public RestResponse<List<User>> listUsers() {
        return RestResponse.ok(userService.findUsers());
    }

    @GetMapping("/{userId}")
    public RestResponse<User> findUserById(@PathVariable(USER_ID) Long userId) {
        return RestResponse.ok(userService.findUserById(userId));
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping("/{userId}/token")
    public RestResponse<JwtPair> getUserToken(@PathVariable(USER_ID) Long userId) {
        return RestResponse.ok(userService.getUserToken(userId));
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN', 'MERCHANT_USER')")
    @GetMapping
    public PageData<User> findUsers(PageLink pageLink) {
        return userService.findUsers(pageLink);
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/tenant/{tenantId}/users")
    public PageData<User> findUsersByTenantId(PageLink pageLink, @PathVariable(TENANT_ID) String tenantId) {
        return userService.findUsersByTenantId(tenantId, pageLink);
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @GetMapping(value = "/merchant/{merchantId}/users")
    public PageData<User> findUsersByMerchantId(PageLink pageLink, @PathVariable(MERCHANT_ID) Long merchantId,
                                                @RequestParam(required = false, defaultValue = "") String textSearch) {
        checkMerchantId(merchantId);
        return userService.findUsersByMerchantIds(new HashSet<>(Arrays.asList(merchantId)), pageLink);
    }
}
