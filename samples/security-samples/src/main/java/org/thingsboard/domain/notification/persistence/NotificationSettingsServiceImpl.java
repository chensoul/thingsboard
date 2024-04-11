package org.thingsboard.domain.notification.persistence;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.common.CacheConstants;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.notification.settings.NotificationSettings;
import org.thingsboard.domain.notification.settings.UserNotificationSettings;
import org.thingsboard.domain.notification.targets.AffectedTenantAdminFilter;
import org.thingsboard.domain.notification.targets.AffectedUserFilter;
import org.thingsboard.domain.notification.targets.AllUserFilter;
import org.thingsboard.domain.notification.targets.NotificationTarget;
import org.thingsboard.domain.notification.targets.OriginatorEntityOwnerUserFilter;
import org.thingsboard.domain.notification.targets.PlatformUserNotificationTargetConfig;
import org.thingsboard.domain.notification.targets.SystemAdminFilter;
import org.thingsboard.domain.notification.targets.TenantAdminFilter;
import org.thingsboard.domain.notification.targets.UserFilter;
import org.thingsboard.domain.notification.targets.UserFilterType;
import org.thingsboard.domain.notification.template.NotificationDeliveryType;
import org.thingsboard.domain.notification.template.NotificationTemplate;
import org.thingsboard.domain.notification.template.NotificationType;
import org.thingsboard.domain.setting.system.model.SystemSetting;
import static org.thingsboard.domain.setting.system.model.SystemSettingType.NOTIFICATION;
import org.thingsboard.domain.setting.system.service.SystemSettingService;
import org.thingsboard.domain.user.model.UserSetting;
import org.thingsboard.domain.user.model.UserSettingType;
import org.thingsboard.domain.user.service.UserSettingService;
import org.thingsboard.server.security.SecurityUtils;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class NotificationSettingsServiceImpl implements NotificationSettingsService {
	private final SystemSettingService systemSettingService;
	private final NotificationTargetService notificationTargetService;
	private final NotificationTemplateService notificationTemplateService;
	private final DefaultNotifications defaultNotifications;
	private final UserSettingService userSettingService;

	@CacheEvict(cacheNames = CacheConstants.NOTIFICATION_SETTING_CACHE, key = "#tenantId")
	@Override
	public void saveNotificationSettings(String tenantId, NotificationSettings settings) {
		if (!tenantId.equals(SYS_TENANT_ID) && settings.getDeliveryMethodsConfigs().containsKey(NotificationDeliveryType.MOBILE_APP.MOBILE_APP)) {
			throw new IllegalArgumentException("Mobile settings can only be configured by system administrator");
		}
		SystemSetting systemSetting = Optional.ofNullable(systemSettingService.findSystemSettingByType(tenantId, NOTIFICATION))
			.orElseGet(() -> {
				SystemSetting newSystemSetting = new SystemSetting();
				newSystemSetting.setTenantId(tenantId);
				newSystemSetting.setType(NOTIFICATION);
				return newSystemSetting;
			});
		systemSetting.setExtra(JacksonUtil.valueToTree(settings));
		systemSettingService.saveSystemSetting(tenantId, systemSetting);
	}

	@Override
	@Cacheable(cacheNames = CacheConstants.NOTIFICATION_SETTING_CACHE, key = "#tenantId")
	public NotificationSettings findNotificationSettings(String tenantId) {
		return Optional.ofNullable(systemSettingService.findSystemSettingByType(tenantId, NOTIFICATION))
			.map(adminSettings -> JacksonUtil.treeToValue(adminSettings.getExtra(), NotificationSettings.class))
			.orElseGet(() -> {
				NotificationSettings settings = new NotificationSettings();
				settings.setDeliveryMethodsConfigs(Collections.emptyMap());
				return settings;
			});
	}

	@Override
	@CacheEvict(cacheNames = CacheConstants.NOTIFICATION_SETTING_CACHE, key = "#tenantId")
	public void deleteNotificationSettings(String tenantId) {
		systemSettingService.deleteSystemSettingByTenantIdAndType(tenantId, NOTIFICATION);
	}

	@Override
	public UserNotificationSettings saveUserNotificationSettings(String tenantId, Long userId, UserNotificationSettings settings) {
		UserSetting userSetting = new UserSetting();
		userSetting.setUserId(userId);
		userSetting.setType(UserSettingType.NOTIFICATION);
		userSetting.setExtra(JacksonUtil.valueToTree(settings));
		userSettingService.saveUserSetting(userSetting);
		return formatUserNotificationSettings(settings);
	}

	@Override
	public UserNotificationSettings getUserNotificationSettings(String tenantId, Long userId, boolean format) {
		return null;
	}

	@Override
	public void createDefaultNotificationConfigs(String tenantId) {
		NotificationTarget allUsers = createTarget(tenantId, "All users", new AllUserFilter(),
			SecurityUtils.isSysTenantId(tenantId) ? "All platform users" : "All users in scope of the tenant");
		NotificationTarget tenantAdmins = createTarget(tenantId, "Tenant administrators", new TenantAdminFilter(),
			SecurityUtils.isSysTenantId(tenantId) ? "All tenant administrators" : "Tenant administrators");

		defaultNotifications.create(tenantId, DefaultNotifications.maintenanceWork);

		if (SecurityUtils.isSysTenantId(tenantId)) {
			NotificationTarget sysAdmins = createTarget(tenantId, "System administrators", new SystemAdminFilter(), "All system administrators");
			NotificationTarget affectedTenantAdmins = createTarget(tenantId, "Affected tenant's administrators", new AffectedTenantAdminFilter(), "");

			defaultNotifications.create(tenantId, DefaultNotifications.entitiesLimitForSysadmin, sysAdmins.getId());
			defaultNotifications.create(tenantId, DefaultNotifications.entitiesLimitForTenant, affectedTenantAdmins.getId());

			defaultNotifications.create(tenantId, DefaultNotifications.apiFeatureWarningForSysadmin, sysAdmins.getId());
			defaultNotifications.create(tenantId, DefaultNotifications.apiFeatureWarningForTenant, affectedTenantAdmins.getId());
			defaultNotifications.create(tenantId, DefaultNotifications.apiFeatureDisabledForSysadmin, sysAdmins.getId());
			defaultNotifications.create(tenantId, DefaultNotifications.apiFeatureDisabledForTenant, affectedTenantAdmins.getId());

			defaultNotifications.create(tenantId, DefaultNotifications.exceededRateLimits, affectedTenantAdmins.getId());
			defaultNotifications.create(tenantId, DefaultNotifications.exceededPerEntityRateLimits, affectedTenantAdmins.getId());
			defaultNotifications.create(tenantId, DefaultNotifications.exceededRateLimitsForSysadmin, sysAdmins.getId());

			defaultNotifications.create(tenantId, DefaultNotifications.newPlatformVersion, sysAdmins.getId());
			return;
		}

		NotificationTarget originatorEntityOwnerUsers = createTarget(tenantId, "Users of the entity owner", new OriginatorEntityOwnerUserFilter(),
			"In case trigger entity (e.g. created device or alarm) is owned by customer, then recipients are this customer's users, otherwise tenant admins");
		NotificationTarget affectedUser = createTarget(tenantId, "Affected user", new AffectedUserFilter(),
			"If rule trigger is an action that affects some user (e.g. alarm assigned to user) - this user");

		defaultNotifications.create(tenantId, DefaultNotifications.newAlarm, tenantAdmins.getId());
		defaultNotifications.create(tenantId, DefaultNotifications.alarmUpdate, tenantAdmins.getId());
		defaultNotifications.create(tenantId, DefaultNotifications.entityAction, tenantAdmins.getId());
		defaultNotifications.create(tenantId, DefaultNotifications.deviceActivity, tenantAdmins.getId());
		defaultNotifications.create(tenantId, DefaultNotifications.alarmComment, tenantAdmins.getId());
		defaultNotifications.create(tenantId, DefaultNotifications.alarmAssignment, affectedUser.getId());
		defaultNotifications.create(tenantId, DefaultNotifications.ruleEngineComponentLifecycleFailure, tenantAdmins.getId());
		defaultNotifications.create(tenantId, DefaultNotifications.edgeConnection, tenantAdmins.getId());
		defaultNotifications.create(tenantId, DefaultNotifications.edgeCommunicationFailures, tenantAdmins.getId());
	}

	@Override
	public void updateDefaultNotificationConfigs(String tenantId) {
		if (SecurityUtils.isSysTenantId(tenantId)) {
			if (notificationTemplateService.findNotificationTemplatesByTenantIdAndTemplateTypes(Pageable.ofSize(10), tenantId,
				List.of(NotificationType.RATE_LIMITS)).getTotalElements() > 0) {
				return;
			}

			NotificationTarget sysAdmins = notificationTargetService.findNotificationTargetsByTenantIdAndUserFilterType(tenantId, UserFilterType.SYSTEM_ADMIN).stream()
				.findFirst().orElseGet(() -> createTarget(tenantId, "System administrators", new SystemAdminFilter(), "All system administrators"));
			NotificationTarget affectedTenantAdmins = notificationTargetService.findNotificationTargetsByTenantIdAndUserFilterType(tenantId, UserFilterType.AFFECTED_TENANT_ADMIN).stream()
				.findFirst().orElseGet(() -> createTarget(tenantId, "Affected tenant's administrators", new AffectedTenantAdminFilter(), ""));

			defaultNotifications.create(tenantId, DefaultNotifications.exceededRateLimits, affectedTenantAdmins.getId());
			defaultNotifications.create(tenantId, DefaultNotifications.exceededPerEntityRateLimits, affectedTenantAdmins.getId());
			defaultNotifications.create(tenantId, DefaultNotifications.exceededRateLimitsForSysadmin, sysAdmins.getId());
		} else {
			var requiredNotificationTypes = List.of(NotificationType.EDGE_CONNECTION, NotificationType.EDGE_COMMUNICATION_FAILURE);
			var existingNotificationTypes = notificationTemplateService.findNotificationTemplatesByTenantIdAndTemplateTypes(Pageable.ofSize(10),
					tenantId, requiredNotificationTypes)
				.getContent()
				.stream()
				.map(NotificationTemplate::getType)
				.collect(Collectors.toSet());

			if (existingNotificationTypes.containsAll(requiredNotificationTypes)) {
				return;
			}

			NotificationTarget tenantAdmins = notificationTargetService.findNotificationTargetsByTenantIdAndUserFilterType(tenantId, UserFilterType.TENANT_ADMIN)
				.stream()
				.findFirst()
				.orElseGet(() -> createTarget(tenantId, "Tenant administrators", new TenantAdminFilter(), "Tenant administrators"));

			for (NotificationType type : requiredNotificationTypes) {
				if (!existingNotificationTypes.contains(type)) {
					switch (type) {
						case EDGE_CONNECTION:
							defaultNotifications.create(tenantId, DefaultNotifications.edgeConnection, tenantAdmins.getId());
							break;
						case EDGE_COMMUNICATION_FAILURE:
							defaultNotifications.create(tenantId, DefaultNotifications.edgeCommunicationFailures, tenantAdmins.getId());
							break;
					}
				}
			}
		}
	}

	private UserNotificationSettings formatUserNotificationSettings(UserNotificationSettings settings) {
		Map<NotificationType, UserNotificationSettings.NotificationPref> prefs = new EnumMap<>(NotificationType.class);
		if (settings != null) {
			prefs.putAll(settings.getPrefs());
		}
		UserNotificationSettings.NotificationPref defaultPref = UserNotificationSettings.NotificationPref.createDefault();
		for (NotificationType notificationType : NotificationType.values()) {
			UserNotificationSettings.NotificationPref pref = prefs.get(notificationType);
			if (pref == null) {
				prefs.put(notificationType, defaultPref);
			} else {
				var enabledDeliveryMethods = new LinkedHashMap<>(pref.getEnabledDeliveryMethods());
				// in case a new delivery method was added to the platform
				UserNotificationSettings.deliveryMethods.forEach(deliveryMethod -> {
					enabledDeliveryMethods.putIfAbsent(deliveryMethod, true);
				});
				pref.setEnabledDeliveryMethods(enabledDeliveryMethods);
			}
		}
		return new UserNotificationSettings(prefs);
	}

	private NotificationTarget createTarget(String tenantId, String name, UserFilter filter, String description) {
		NotificationTarget target = new NotificationTarget();
		target.setTenantId(tenantId);
		target.setName(name);

		PlatformUserNotificationTargetConfig targetConfig = new PlatformUserNotificationTargetConfig();
		targetConfig.setUserFilter(filter);
		target.setConfig(targetConfig);
		return notificationTargetService.saveNotificationTarget(target);
	}
}
