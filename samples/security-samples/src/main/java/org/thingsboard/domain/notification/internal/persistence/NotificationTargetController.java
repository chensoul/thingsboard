package org.thingsboard.domain.notification.internal.persistence;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
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
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.notification.internal.targets.NotificationTarget;
import org.thingsboard.domain.notification.internal.template.NotificationType;
import org.thingsboard.domain.user.User;
import org.thingsboard.server.security.SecurityUser;

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
