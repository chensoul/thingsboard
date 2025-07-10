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
package org.thingsboard.domain.setting.internal;

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
import static org.thingsboard.common.CacheConstants.NOTIFICATION_SETTING_CACHE;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.notification.internal.persistence.DefaultNotifications;
import org.thingsboard.domain.notification.internal.persistence.NotificationTargetService;
import org.thingsboard.domain.notification.internal.persistence.NotificationTemplateService;
import org.thingsboard.domain.notification.internal.targets.AffectedTenantAdminFilter;
import org.thingsboard.domain.notification.internal.targets.AffectedUserFilter;
import org.thingsboard.domain.notification.internal.targets.AllUserFilter;
import org.thingsboard.domain.notification.internal.targets.NotificationTarget;
import org.thingsboard.domain.notification.internal.targets.OriginatorEntityOwnerUserFilter;
import org.thingsboard.domain.notification.internal.targets.PlatformUserNotificationTargetConfig;
import org.thingsboard.domain.notification.internal.targets.SystemAdminFilter;
import org.thingsboard.domain.notification.internal.targets.TenantAdminFilter;
import org.thingsboard.domain.notification.UserFilter;
import org.thingsboard.domain.notification.internal.targets.UserFilterType;
import org.thingsboard.domain.notification.internal.template.NotificationDeliveryMethod;
import org.thingsboard.domain.notification.internal.template.NotificationTemplate;
import org.thingsboard.domain.notification.internal.template.NotificationType;
import org.thingsboard.domain.setting.SystemSetting;
import static org.thingsboard.domain.setting.SystemSettingType.NOTIFICATION;
import org.thingsboard.domain.setting.SystemSettingService;
import org.thingsboard.domain.setting.NotificationSetting;
import org.thingsboard.domain.setting.NotificationSettingService;
import org.thingsboard.domain.setting.UserNotificationSetting;
import org.thingsboard.domain.user.UserSetting;
import org.thingsboard.domain.user.UserSettingType;
import org.thingsboard.domain.user.UserSettingService;
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
public class NotificationSettingServiceImpl implements NotificationSettingService {
	private final SystemSettingService systemSettingService;
	private final NotificationTargetService notificationTargetService;
	private final NotificationTemplateService notificationTemplateService;
	private final DefaultNotifications defaultNotifications;
	private final UserSettingService userSettingService;

	@CacheEvict(cacheNames = NOTIFICATION_SETTING_CACHE, key = "#tenantId")
	@Override
	public void saveNotificationSetting(String tenantId, NotificationSetting settings) {
		if (!tenantId.equals(SYS_TENANT_ID) && settings.getDeliveryMethodsConfigs().containsKey(NotificationDeliveryMethod.MOBILE_APP.MOBILE_APP)) {
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
	@Cacheable(cacheNames = NOTIFICATION_SETTING_CACHE, key = "#tenantId")
	public NotificationSetting findNotificationSetting(String tenantId) {
		return Optional.ofNullable(systemSettingService.findSystemSettingByType(tenantId, NOTIFICATION))
			.map(adminSettings -> JacksonUtil.treeToValue(adminSettings.getExtra(), NotificationSetting.class))
			.orElseGet(() -> {
				NotificationSetting settings = new NotificationSetting();
				settings.setDeliveryMethodsConfigs(Collections.emptyMap());
				return settings;
			});
	}

	@Override
	@CacheEvict(cacheNames = NOTIFICATION_SETTING_CACHE, key = "#tenantId")
	public void deleteNotificationSetting(String tenantId) {
		systemSettingService.deleteSystemSettingByTenantIdAndType(tenantId, NOTIFICATION);
	}

	@Override
	public UserNotificationSetting saveUserNotificationSetting(String tenantId, Long userId, UserNotificationSetting settings) {
		UserSetting userSetting = new UserSetting();
		userSetting.setUserId(userId);
		userSetting.setType(UserSettingType.NOTIFICATION);
		userSetting.setExtra(JacksonUtil.valueToTree(settings));
		userSettingService.saveUserSetting(userSetting);
		return formatUserNotificationSettings(settings);
	}

	@Override
	public UserNotificationSetting getUserNotificationSetting(String tenantId, Long userId, boolean format) {
		return null;
	}

	@Override
	public void createDefaultNotificationConfig(String tenantId) {
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
	public void updateDefaultNotificationConfig(String tenantId) {
		if (SecurityUtils.isSysTenantId(tenantId)) {
			if (notificationTemplateService.findNotificationTemplatesByTenantIdAndTemplateTypes(Pageable.ofSize(10), tenantId,
				List.of(NotificationType.RATE_LIMITS)).getTotalElements() > 0) {
				return;
			}

			NotificationTarget sysAdmins = notificationTargetService.findNotificationTargetsByTenantIdAndUserFilterType(tenantId, UserFilterType.SYS_ADMIN).stream()
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

	private UserNotificationSetting formatUserNotificationSettings(UserNotificationSetting settings) {
		Map<NotificationType, UserNotificationSetting.NotificationPref> prefs = new EnumMap<>(NotificationType.class);
		if (settings != null) {
			prefs.putAll(settings.getPrefs());
		}
		UserNotificationSetting.NotificationPref defaultPref = UserNotificationSetting.NotificationPref.createDefault();
		for (NotificationType notificationType : NotificationType.values()) {
			UserNotificationSetting.NotificationPref pref = prefs.get(notificationType);
			if (pref == null) {
				prefs.put(notificationType, defaultPref);
			} else {
				var enabledDeliveryMethods = new LinkedHashMap<>(pref.getEnabledDeliveryMethods());
				// in case a new delivery method was added to the platform
				UserNotificationSetting.deliveryMethods.forEach(deliveryMethod -> {
					enabledDeliveryMethods.putIfAbsent(deliveryMethod, true);
				});
				pref.setEnabledDeliveryMethods(enabledDeliveryMethods);
			}
		}
		return new UserNotificationSetting(prefs);
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
