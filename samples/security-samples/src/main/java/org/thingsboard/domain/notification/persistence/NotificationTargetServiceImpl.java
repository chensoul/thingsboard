package org.thingsboard.domain.notification.persistence;

import static com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isNotEmpty;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
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
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.user.service.UserService;
import org.thingsboard.server.security.SecurityUtils;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

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
	public Page<User> findRecipientsForNotificationTargetConfig(Pageable pageable, String tenantId, Long id) {
		NotificationTargetConfig targetConfig = findNotificationTargetById(id).getConfig();
		if (!(targetConfig instanceof PlatformUserNotificationTargetConfig)) {
			return Page.empty();
		}
		checkTargetUsers(SecurityUtils.getCurrentUser(), targetConfig);

		PlatformUserNotificationTargetConfig config = (PlatformUserNotificationTargetConfig) targetConfig;
		UserFilter userFilter = config.getUserFilter();
		switch (userFilter.getType()) {
			case USER_LIST: {
				UserListFilter filter = (UserListFilter) userFilter;
				return userService.findUsers(pageable, filter.getUserIds(), null);
			}
			case MERCHANT_USER: {
				if (tenantId.equals(SYS_TENANT_ID)) {
					throw new IllegalArgumentException("Customer users target is not supported for system administrator");
				}
				MerchantUserFilter filter = (MerchantUserFilter) userFilter;
				return userService.findMerchantUsers(pageable, filter.getMerchantIds(), null);
			}
			case TENANT_ADMIN: {
				TenantAdminFilter filter = (TenantAdminFilter) userFilter;
				if (!tenantId.equals(SYS_TENANT_ID)) {
					return userService.findTenantAdminsByTenantsIds(pageable, Set.of(tenantId));
				} else {
					if (isNotEmpty(filter.getTenantIds())) {
						return userService.findTenantAdminsByTenantsIds(pageable, filter.getTenantIds());
					} else {
						return userService.findAllTenantAdmins(pageable);
					}
				}
			}
			case TENANT_USER:
				if (!tenantId.equals(SYS_TENANT_ID)) {
					return userService.findTenantUsers(pageable, tenantId, null);
				} else {
					return userService.findUsers(pageable, null, null);
				}
			case SYSTEM_ADMIN:
				return userService.findAllSysAdmins(pageable);
			case ALL_USER: {
				if (!tenantId.equals(SYS_TENANT_ID)) {
					return userService.findTenantUsers(pageable, tenantId, null);
				} else {
					return userService.findUsers(pageable, null, null);
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
	public Page<NotificationTarget> findNotificationTargetsByTenantId(Pageable pageable, String tenantId, NotificationType notificationType, String textSearch) {
		return notificationTargetDao.findNotificationTargetsByTenantId(pageable, tenantId, notificationType, textSearch);
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
			case SYSTEM_ADMIN:
				throw new AccessDeniedException("");
		}
	}
}
