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
package com.chensoul.system.domain.setting.service.impl;

import com.chensoul.json.JacksonUtils;
import static com.chensoul.system.CacheConstants.NOTIFICATION_SETTING_CACHE;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.notification.domain.targets.AffectedTenantAdminFilter;
import com.chensoul.system.domain.notification.domain.targets.AffectedUserFilter;
import com.chensoul.system.domain.notification.domain.targets.AllUserFilter;
import com.chensoul.system.domain.notification.domain.targets.NotificationTarget;
import com.chensoul.system.domain.notification.domain.targets.OriginatorEntityOwnerUserFilter;
import com.chensoul.system.domain.notification.domain.targets.PlatformUserNotificationTargetConfig;
import com.chensoul.system.domain.notification.domain.targets.SystemAdminFilter;
import com.chensoul.system.domain.notification.domain.targets.TenantAdminFilter;
import com.chensoul.system.domain.notification.domain.targets.UserFilter;
import com.chensoul.system.domain.notification.domain.targets.UserFilterType;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import com.chensoul.system.domain.notification.domain.template.NotificationTemplate;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import com.chensoul.system.domain.notification.mybatis.DefaultNotifications;
import com.chensoul.system.domain.notification.service.NotificationTargetService;
import com.chensoul.system.domain.notification.service.NotificationTemplateService;
import com.chensoul.system.domain.setting.domain.NotificationSetting;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import static com.chensoul.system.domain.setting.domain.SystemSettingType.NOTIFICATION;
import com.chensoul.system.domain.setting.domain.UserNotificationSetting;
import com.chensoul.system.domain.setting.service.NotificationSettingService;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.domain.user.service.UserSettingService;
import com.chensoul.system.infrastructure.security.util.SecurityUtils;
import com.chensoul.system.user.domain.UserSetting;
import com.chensoul.system.user.domain.UserSettingType;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        systemSetting.setExtra(JacksonUtils.valueToTree(settings));
        systemSettingService.saveSystemSetting(tenantId, systemSetting);
    }

    @Override
    @Cacheable(cacheNames = NOTIFICATION_SETTING_CACHE, key = "#tenantId")
    public NotificationSetting findNotificationSetting(String tenantId) {
        return Optional.ofNullable(systemSettingService.findSystemSettingByType(tenantId, NOTIFICATION))
            .map(adminSettings -> JacksonUtils.treeToValue(adminSettings.getExtra(), NotificationSetting.class))
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
        userSetting.setExtra(JacksonUtils.valueToTree(settings));
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
                Arrays.asList(NotificationType.RATE_LIMITS)).getTotalElements() > 0) {
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
            List<NotificationType> requiredNotificationTypes = Arrays.asList(NotificationType.EDGE_CONNECTION, NotificationType.EDGE_COMMUNICATION_FAILURE);
            Set<NotificationType> existingNotificationTypes = notificationTemplateService.findNotificationTemplatesByTenantIdAndTemplateTypes(Pageable.ofSize(10),
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
                Map enabledDeliveryMethods = new LinkedHashMap<>(pref.getEnabledDeliveryMethods());
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
