package org.thingsboard.domain.notification.persistence;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.thingsboard.common.dao.page.PageData;
import org.thingsboard.common.dao.page.PageLink;
import org.thingsboard.domain.notification.targets.MerchantUserFilter;
import org.thingsboard.domain.notification.targets.NotificationTarget;
import org.thingsboard.domain.notification.targets.NotificationTargetConfig;
import org.thingsboard.domain.notification.targets.NotificationTargetType;
import org.thingsboard.domain.notification.targets.PlatformUserNotificationTargetConfig;
import org.thingsboard.domain.notification.targets.TenantAdminFilter;
import org.thingsboard.domain.notification.targets.UserFilter;
import org.thingsboard.domain.notification.targets.UserFilterType;
import org.thingsboard.domain.notification.targets.UserListFilter;
import org.thingsboard.domain.notification.template.NotificationType;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.service.UserService;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;
import org.thingsboard.server.security.SecurityUtils;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class NotificationTargetServiceImpl implements NotificationTargetService {
	private final NotificationTargetDao notificationTargetDao;
	private final UserService userService;

	@SneakyThrows
	@Override
	public NotificationTarget saveNotificationTarget(NotificationTarget notificationTarget) {
		NotificationTargetConfig targetConfig = notificationTarget.getConfig();
		if (targetConfig.getType() == NotificationTargetType.PLATFORM_USER) {
			checkTargetUsers(SecurityUtils.getCurrentUser(), targetConfig);
		}

		return notificationTargetDao.save(notificationTarget);
	}

	@Override
	public NotificationTarget findNotificationTargetById(Long id) {
		return notificationTargetDao.findById(id);
	}

	@Override
	public PageData<User> findRecipientsForNotificationTargetConfig(String tenantId, Long id, PageLink pageLink) {
		NotificationTargetConfig targetConfig = findNotificationTargetById(id).getConfig();
		if (!(targetConfig instanceof PlatformUserNotificationTargetConfig)) {
			return PageData.empty();
		}
		checkTargetUsers(SecurityUtils.getCurrentUser(), targetConfig);

		PlatformUserNotificationTargetConfig config = (PlatformUserNotificationTargetConfig) targetConfig;
		UserFilter userFilter = config.getUserFilter();
		switch (userFilter.getType()) {
			case USER_LIST: {
				UserListFilter filter = (UserListFilter) userFilter;
				return userService.findUsersByIds(filter.getUserIds(), pageLink);
			}
			case MERCHANT_USER: {
				if (tenantId.equals(SYS_TENANT_ID)) {
					throw new IllegalArgumentException("Customer users target is not supported for system administrator");
				}
				MerchantUserFilter filter = (MerchantUserFilter) userFilter;
				return userService.findUsersByMerchantIds(filter.getMerchantIds(), pageLink);
			}
			case TENANT_ADMIN: {
				TenantAdminFilter filter = (TenantAdminFilter) userFilter;
				if (!tenantId.equals(SYS_TENANT_ID) || isNotEmpty(filter.getTenantIds())) {
					return userService.findUsersByTenantIdsAndAuthority(filter.getTenantIds(), Authority.TENANT_ADMIN, pageLink);
				} else {
					return userService.findUsersByTenantIdsAndAuthority(filter.getTenantIds(), Authority.TENANT_ADMIN, pageLink);
				}
			}
			case TENANT_USER:
				if (!tenantId.equals(SYS_TENANT_ID)) {
					return userService.findUsersByTenantIdsAndAuthority(Set.of(tenantId), Authority.MERCHANT_USER, pageLink);
				} else {
					return userService.findUsersByAuthority(Authority.MERCHANT_USER, pageLink);
				}
			case SYS_ADMIN:
				return userService.findUsersByTenantIdsAndAuthority(Set.of(SYS_TENANT_ID), Authority.SYS_ADMIN, pageLink);
			case ALL_USER: {
				if (!tenantId.equals(SYS_TENANT_ID)) {
					return userService.findUsersByTenantId(tenantId, pageLink);
				} else {
					return userService.findUsersByIds(null, pageLink);
				}
			}
			default:
				throw new IllegalArgumentException("Recipient type not supported");
		}
	}

	@Override
	public List<NotificationTarget> findNotificationTargetsByTenantIdAndIds(String tenantId, Set<Long> ids) {
		return notificationTargetDao.findByTenantIdAndIds(tenantId, ids);
	}

	@Override
	public List<NotificationTarget> findNotificationTargetsByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType) {
		//TODO
		return List.of();
	}

	@Override
	public PageData<NotificationTarget> findNotificationTargetsByTenantId(String tenantId, NotificationType notificationType, PageLink pageLink) {
		return notificationTargetDao.findNotificationTargetsByTenantId(tenantId, notificationType, pageLink);
	}

	@Override
	public void deleteNotificationTargetById(Long id) {
		notificationTargetDao.removeById(id);
	}

	private void checkTargetUsers(SecurityUser user, NotificationTargetConfig targetConfig) {
		if (user.isSystemAdmin()) {
			return;
		}
		// PE: generic permission for users
		UserFilter usersFilter = ((PlatformUserNotificationTargetConfig) targetConfig).getUserFilter();
		switch (usersFilter.getType()) {
			case USER_LIST:
				for (Long recipientId : ((UserListFilter) usersFilter).getUserIds()) {
				}
				break;
			case MERCHANT_USER:
				Set<Long> merchantIds = ((MerchantUserFilter) usersFilter).getMerchantIds();
				break;
			case SYS_ADMIN:
				throw new AccessDeniedException("");
		}
	}
}
