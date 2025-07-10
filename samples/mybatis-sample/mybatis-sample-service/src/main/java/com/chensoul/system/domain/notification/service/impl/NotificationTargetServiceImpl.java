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
package com.chensoul.system.domain.notification.service.impl;

import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.notification.domain.targets.MerchantUserFilter;
import com.chensoul.system.domain.notification.domain.targets.NotificationTarget;
import com.chensoul.system.domain.notification.domain.targets.NotificationTargetConfig;
import com.chensoul.system.domain.notification.domain.targets.NotificationTargetType;
import com.chensoul.system.domain.notification.domain.targets.PlatformUserNotificationTargetConfig;
import com.chensoul.system.domain.notification.domain.targets.TenantAdminFilter;
import com.chensoul.system.domain.notification.domain.targets.UserFilter;
import com.chensoul.system.domain.notification.domain.targets.UserFilterType;
import com.chensoul.system.domain.notification.domain.targets.UserListFilter;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import com.chensoul.system.domain.notification.mybatis.NotificationTargetDao;
import com.chensoul.system.domain.notification.service.NotificationTargetService;
import com.chensoul.system.domain.user.service.UserService;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.infrastructure.security.util.SecurityUtils;
import com.chensoul.system.user.domain.Authority;
import com.chensoul.system.user.domain.User;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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
                    return userService.findUsersByTenantIdsAndAuthority(new HashSet<>(Arrays.asList(tenantId)), Authority.MERCHANT_USER, pageLink);
                } else {
                    return userService.findUsersByAuthority(Authority.MERCHANT_USER, pageLink);
                }
            case SYS_ADMIN:
                return userService.findUsersByTenantIdsAndAuthority(new HashSet<>(Arrays.asList(SYS_TENANT_ID)), Authority.SYS_ADMIN, pageLink);
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
        return Arrays.asList();
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
