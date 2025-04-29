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
package com.chensoul.system.domain.notification.controller;

import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.system.domain.notification.domain.targets.NotificationTarget;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import com.chensoul.system.domain.notification.service.NotificationTargetService;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.User;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationTargetController {
    private final NotificationTargetService notificationTargetService;

    @PostMapping("/target")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public NotificationTarget saveNotificationTarget(@RequestBody @Valid NotificationTarget notificationTarget,
                                                     @AuthenticationPrincipal SecurityUser user) throws Exception {
        notificationTarget.setTenantId(user.getTenantId());
        return notificationTargetService.saveNotificationTarget(notificationTarget);
    }

    @GetMapping("/target/{id}")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public NotificationTarget getNotificationTargetById(@PathVariable Long id) {
        return notificationTargetService.findNotificationTargetById(id);
    }

    @GetMapping("/target/{id}/recipients")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public PageData<User> getRecipientsForNotificationTargetConfig(PageLink pageLink, @PathVariable Long id, @AuthenticationPrincipal SecurityUser user) {
        return notificationTargetService.findRecipientsForNotificationTargetConfig(user.getTenantId(), id, pageLink);
    }

    @GetMapping(value = "/targets", params = {"ids"})
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public List<NotificationTarget> getNotificationTargetsByIds(@RequestParam("ids") Set<Long> ids,
                                                                @AuthenticationPrincipal SecurityUser user) {
        return notificationTargetService.findNotificationTargetsByTenantIdAndIds(user.getTenantId(), ids);
    }

    @GetMapping("/targets")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public PageData<NotificationTarget> getNotificationTargets(PageLink pageLink,
                                                               @RequestParam(required = false) NotificationType notificationType,
                                                               @AuthenticationPrincipal SecurityUser user) {
        return notificationTargetService.findNotificationTargetsByTenantId(user.getTenantId(), notificationType, pageLink);
    }

    @DeleteMapping("/target/{id}")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    public void deleteNotificationTargetById(@PathVariable Long id) throws Exception {
        notificationTargetService.deleteNotificationTargetById(id);
    }
}
