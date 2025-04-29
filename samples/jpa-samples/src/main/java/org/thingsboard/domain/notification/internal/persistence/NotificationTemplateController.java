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
package org.thingsboard.domain.notification.internal.persistence;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.thingsboard.domain.notification.internal.template.NotificationTemplate;
import org.thingsboard.domain.notification.internal.template.NotificationType;
import static org.thingsboard.server.security.SecurityUtils.getTenantId;
import org.thingsboard.server.security.SecurityUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationTemplateController {
	private final NotificationTemplateService notificationTemplateService;

	@PostMapping("/template")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationTemplate saveNotificationTemplate(@RequestBody @Valid NotificationTemplate notificationTemplate) throws Exception {
		notificationTemplate.setTenantId(getTenantId());
		return notificationTemplateService.saveNotificationTemplate(notificationTemplate);
	}

	@GetMapping("/template/{id}")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationTemplate getNotificationTemplateById(@PathVariable Long id) {
		return notificationTemplateService.findNotificationTemplateById(id);
	}

	@GetMapping("/templates")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public Page<NotificationTemplate> getNotificationTemplates(Pageable pageable,
															   @RequestParam(required = false) NotificationType[] notificationTypes,
															   @AuthenticationPrincipal SecurityUser user) {
		if (notificationTypes == null || notificationTypes.length == 0) {
			notificationTypes = NotificationType.values();
		}
		return notificationTemplateService.findNotificationTemplatesByTenantIdAndTemplateTypes(pageable, user.getTenantId(), List.of(notificationTypes));
	}

	@DeleteMapping("/template/{id}")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public void deleteNotificationTemplateById(@PathVariable Long id) throws Exception {
		notificationTemplateService.deleteNotificationTemplateById(id);
	}
}
