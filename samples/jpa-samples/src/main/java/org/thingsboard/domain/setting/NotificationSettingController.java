package org.thingsboard.domain.setting;

import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.domain.notification.NotificationCenter;
import org.thingsboard.domain.notification.internal.template.NotificationDeliveryMethod;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Slf4j
public class NotificationSettingController {
	private final NotificationSettingService notificationSettingService;
	private final NotificationCenter notificationCenter;

	@PostMapping("/notificationSetting")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationSetting saveNotificationSettings(@RequestBody @Valid NotificationSetting notificationSetting,
														@AuthenticationPrincipal SecurityUser user) {
		String tenantId = user.isSystemAdmin() ? SYS_TENANT_ID : user.getTenantId();
		notificationSettingService.saveNotificationSetting(tenantId, notificationSetting);
		return notificationSetting;
	}

	@GetMapping("/notificationSetting")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	public NotificationSetting getNotificationSettings(@AuthenticationPrincipal SecurityUser user) {
		String tenantId = user.isSystemAdmin() ? SYS_TENANT_ID : user.getTenantId();
		return notificationSettingService.findNotificationSetting(tenantId);
	}

	@GetMapping("/notificationSetting/deliveryType")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public Set<NotificationDeliveryMethod> getAvailableDeliveryMethods(@AuthenticationPrincipal SecurityUser user) {
		return notificationCenter.getAvailableDeliveryTypes(user.getTenantId());
	}

	@PostMapping("/notificationSetting/user")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public UserNotificationSetting saveUserNotificationSettings(@RequestBody @Valid UserNotificationSetting settings,
																@AuthenticationPrincipal SecurityUser user) {
		return notificationSettingService.saveUserNotificationSetting(user.getTenantId(), user.getId(), settings);
	}

	@GetMapping("/notificationSetting/user")
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER')")
	public UserNotificationSetting getUserNotificationSettings(@AuthenticationPrincipal SecurityUser user) {
		return notificationSettingService.getUserNotificationSetting(user.getTenantId(), user.getId(), true);
	}
}
