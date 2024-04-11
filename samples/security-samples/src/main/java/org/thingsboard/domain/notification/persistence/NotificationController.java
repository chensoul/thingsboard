/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.notification.persistence;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.notification.Notification;
import org.thingsboard.domain.notification.NotificationCenter;
import org.thingsboard.domain.notification.NotificationContext;
import org.thingsboard.domain.notification.NotificationRecipient;
import org.thingsboard.domain.notification.NotificationRequest;
import org.thingsboard.domain.notification.NotificationStatus;
import org.thingsboard.domain.notification.settings.NotificationSettings;
import org.thingsboard.domain.notification.settings.UserNotificationSettings;
import org.thingsboard.domain.notification.targets.NotificationTarget;
import org.thingsboard.domain.notification.targets.NotificationTargetType;
import org.thingsboard.domain.notification.template.NotificationDeliveryTemplate;
import org.thingsboard.domain.notification.template.NotificationDeliveryType;
import org.thingsboard.domain.notification.template.NotificationTemplate;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

	private final NotificationService notificationService;
	private final NotificationRequestService notificationRequestService;
	private final NotificationTemplateService notificationTemplateService;
	private final NotificationTargetService notificationTargetService;
	private final NotificationSettingsService notificationSettingsService;
	private final NotificationCenter notificationCenter;

	@GetMapping("/notifications")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public Page<Notification> getNotifications(Pageable pageable,
											   @RequestParam(defaultValue = "false") boolean unreadOnly,
											   @AuthenticationPrincipal SecurityUser user) {
		return notificationService.findNotificationsByRecipientIdAndReadStatus(pageable, user.getId(), unreadOnly ? NotificationStatus.SENT : null, null);
	}

	@PutMapping("/notification/{id}/read")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public void markNotificationAsRead(@PathVariable Long id,
									   @AuthenticationPrincipal SecurityUser user) {
		notificationService.markNotificationAsRead(user.getId(), id);
	}

	@PutMapping("/notifications/read")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public void markAllNotificationsAsRead(@AuthenticationPrincipal SecurityUser user) {
		notificationService.markAllNotificationsAsRead(user.getId());
	}

	@DeleteMapping("/notification/{id}")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public void deleteNotification(@PathVariable Long id,
								   @AuthenticationPrincipal SecurityUser user) {
		notificationService.deleteNotification(user.getId(), id);
	}

	@PostMapping("/notification/request")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationRequest createNotificationRequest(@RequestBody @Valid NotificationRequest notificationRequest,
														 @AuthenticationPrincipal SecurityUser user) throws Exception {
		if (notificationRequest.getId() != null) {
			throw new IllegalArgumentException("Notification request cannot be updated. You may only cancel/delete it");
		}
		notificationRequest.setTenantId(user.getTenantId());

		notificationRequest.setEntityId(user.getId());
		notificationRequest.setInfo(null);
		notificationRequest.setRuleId(null);
		notificationRequest.setStatus(null);
		notificationRequest.setStats(null);

		return null;
	}

	@PostMapping("/notification/request/preview")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationRequestPreview getNotificationRequestPreview(Pageable pageable, @RequestBody @Valid NotificationRequest request,
																	@RequestParam(defaultValue = "20") int recipientsPreviewSize,
																	@AuthenticationPrincipal SecurityUser user) {
		// PE: generic permission
		NotificationTemplate template;
		if (request.getTemplateId() != null) {
			template = notificationTemplateService.findNotificationTemplateById(request.getTemplateId());
		} else {
			template = request.getTemplate();
		}
		if (template == null) {
			throw new IllegalArgumentException("Template is missing");
		}
		request.setEntityId(user.getId());
		List<NotificationTarget> targets = request.getTargets().stream()
			.map(target -> notificationTargetService.findNotificationTargetById(target))
			.sorted(Comparator.comparing(target -> target.getConfig().getType()))
			.collect(Collectors.toList());

		NotificationRequestPreview preview = new NotificationRequestPreview();

		Set<String> recipientsPreview = new LinkedHashSet<>();
		Map<String, Integer> recipientsCountByTarget = new LinkedHashMap<>();
		Map<NotificationTargetType, NotificationRecipient> firstRecipient = new HashMap<>();
		for (NotificationTarget target : targets) {
			int recipientsCount;
			List<NotificationRecipient> recipientsPart = List.of();
			NotificationTargetType targetType = target.getConfig().getType();
			switch (targetType) {
				case PLATFORM_USER: {
					Page<User> recipients = notificationTargetService.findRecipientsForNotificationTargetConfig(pageable, user.getTenantId(), target.getId());
					recipientsCount = (int) recipients.getTotalElements();
					recipientsPart = recipients.getContent().stream().map(r -> (NotificationRecipient) r).collect(Collectors.toList());
					break;
				}
				case SLACK: {
					recipientsCount = 1;
//					recipientsPart = List.of(((SlackNotificationTargetConfig) target.getConfig()).getConversation());
					break;
				}
				case MICROSOFT_TEAM: {
					recipientsCount = 1;
//					recipientsPart = List.of(((MicrosoftTeamsNotificationTargetConfig) target.getConfig()));
					break;
				}
				default:
					throw new IllegalArgumentException("Target type " + targetType + " not supported");
			}
			firstRecipient.putIfAbsent(targetType, !recipientsPart.isEmpty() ? recipientsPart.get(0) : null);
			for (NotificationRecipient recipient : recipientsPart) {
				if (recipientsPreview.size() < recipientsPreviewSize) {
					String title = recipient.getName();
//					if (recipient instanceof SlackConversation) {
//						title = ((SlackConversation) recipient).getPointer() + title;
//					} else
					if (recipient instanceof User) {
						if (!title.equals(recipient.getEmail())) {
							title += " (" + recipient.getEmail() + ")";
						}
					}
					recipientsPreview.add(title);
				} else {
					break;
				}
			}
			recipientsCountByTarget.put(target.getName(), recipientsCount);
		}
		preview.setRecipientsPreview(recipientsPreview);
		preview.setRecipientsCountByTarget(recipientsCountByTarget);
		preview.setTotalRecipientsCount(recipientsCountByTarget.values().stream().mapToInt(Integer::intValue).sum());

		Set<NotificationDeliveryType> deliveryTypes = template.getConfig().getDeliveryTemplates().entrySet()
			.stream().filter(entry -> entry.getValue().isEnabled()).map(Map.Entry::getKey).collect(Collectors.toSet());
		NotificationContext ctx = NotificationContext.builder()
			.tenantId(user.getTenantId())
			.request(request)
			.deliveryTypes(deliveryTypes)
			.template(template)
			.settings(null)
			.build();
		Map<NotificationDeliveryType, NotificationDeliveryTemplate> processedTemplates = ctx.getDeliveryTypes().stream()
			.collect(Collectors.toMap(m -> m, deliveryMethod -> {
				NotificationTargetType targetType = NotificationTargetType.forDeliveryMethod(deliveryMethod);
				return ctx.getProcessedTemplate(deliveryMethod, firstRecipient.get(targetType));
			}));
		preview.setProcessedTemplates(processedTemplates);

		return preview;
	}

	@GetMapping("/notification/request/{id}")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationRequestInfo getNotificationRequestById(@PathVariable Long id) {
		return notificationRequestService.findNotificationRequestInfoById(id);
	}

	@GetMapping("/notification/requests")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public Page<NotificationRequestInfo> getNotificationRequests(Pageable pageable,
																 @AuthenticationPrincipal SecurityUser user) {
		return notificationRequestService.findNotificationRequestsInfosByTenantIdAndOriginatorType(user.getTenantId(), EntityType.USER);
	}

	@DeleteMapping("/notification/request/{id}")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public void deleteNotificationRequest(@PathVariable Long id) throws Exception {
		notificationRequestService.deleteNotificationRequest(id);
	}


	@PostMapping("/notification/settings")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationSettings saveNotificationSettings(@RequestBody @Valid NotificationSettings notificationSettings,
														 @AuthenticationPrincipal SecurityUser user) {
		String tenantId = user.isSystemAdmin() ? SYS_TENANT_ID : user.getTenantId();
		notificationSettingsService.saveNotificationSettings(tenantId, notificationSettings);
		return notificationSettings;
	}


	@GetMapping("/notification/settings")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationSettings getNotificationSettings(@AuthenticationPrincipal SecurityUser user) {
		String tenantId = user.isSystemAdmin() ? SYS_TENANT_ID : user.getTenantId();
		return notificationSettingsService.findNotificationSettings(tenantId);
	}


	@GetMapping("/notification/deliveryTypes")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public Set<NotificationDeliveryType> getAvailableDeliveryMethods(@AuthenticationPrincipal SecurityUser user) {
		return notificationCenter.getAvailableDeliveryTypes(user.getTenantId());
	}

	@PostMapping("/notification/settings/user")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public UserNotificationSettings saveUserNotificationSettings(@RequestBody @Valid UserNotificationSettings settings,
																 @AuthenticationPrincipal SecurityUser user) {
		return notificationSettingsService.saveUserNotificationSettings(user.getTenantId(), user.getId(), settings);
	}

	@GetMapping("/notification/settings/user")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public UserNotificationSettings getUserNotificationSettings(@AuthenticationPrincipal SecurityUser user) {
		return notificationSettingsService.getUserNotificationSettings(user.getTenantId(), user.getId(), true);
	}

}
