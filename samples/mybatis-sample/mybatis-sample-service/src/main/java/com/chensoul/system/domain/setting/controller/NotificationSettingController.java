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
package com.chensoul.system.domain.setting.controller;

import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import com.chensoul.system.domain.notification.service.NotificationCenter;
import com.chensoul.system.domain.setting.domain.NotificationSetting;
import com.chensoul.system.domain.setting.domain.UserNotificationSetting;
import com.chensoul.system.domain.setting.service.NotificationSettingService;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import java.util.Set;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@RequestMapping("/api/systems/setting")
@RequiredArgsConstructor
@Slf4j
public class NotificationSettingController {
    private final NotificationSettingService notificationSettingService;
    private final NotificationCenter notificationCenter;

    @PostMapping("/notification")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public NotificationSetting saveNotificationSetting(@RequestBody @Valid NotificationSetting notificationSetting,
                                                       @AuthenticationPrincipal SecurityUser user) {
        String tenantId = user.isSystemAdmin() ? SYS_TENANT_ID : user.getTenantId();
        notificationSettingService.saveNotificationSetting(tenantId, notificationSetting);
        return notificationSetting;
    }

    @GetMapping("/notification")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public NotificationSetting getNotificationSettings(@AuthenticationPrincipal SecurityUser user) {
        String tenantId = user.isSystemAdmin() ? SYS_TENANT_ID : user.getTenantId();
        return notificationSettingService.findNotificationSetting(tenantId);
    }

    @GetMapping("/notification/deliveryType")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public Set<NotificationDeliveryMethod> getAvailableDeliveryMethods(@AuthenticationPrincipal SecurityUser user) {
        return notificationCenter.getAvailableDeliveryTypes(user.getTenantId());
    }

    @PostMapping("/notification/user")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public UserNotificationSetting saveUserNotificationSettings(@RequestBody @Valid UserNotificationSetting settings,
                                                                @AuthenticationPrincipal SecurityUser user) {
        return notificationSettingService.saveUserNotificationSetting(user.getTenantId(), user.getId(), settings);
    }

    @GetMapping("/notification/user")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
    public UserNotificationSetting getUserNotificationSettings(@AuthenticationPrincipal SecurityUser user) {
        return notificationSettingService.getUserNotificationSetting(user.getTenantId(), user.getId(), true);
    }
}
