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

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thingsboard.common.dao.Dao;
import org.thingsboard.common.dao.TenantEntityDao;
import org.thingsboard.domain.notification.targets.NotificationTarget;
import org.thingsboard.domain.notification.targets.NotificationTargetType;
import org.thingsboard.domain.notification.targets.UserFilterType;
import org.thingsboard.domain.notification.template.NotificationType;

public interface NotificationTargetDao extends Dao<NotificationTarget>, TenantEntityDao {
	List<NotificationTarget> findByTenantIdAndIds(String tenantId, Set<Long> ids);

	List<NotificationTarget> findByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType);

	Page<NotificationTarget> findByTenantIdAndSupportedNotificationType(Pageable pageable, String tenantId, NotificationTargetType notificationTargetType);

	void removeByTenantId(String tenantId);

	Page<NotificationTarget> findNotificationTargetsByTenantId(Pageable pageable, String tenantId, NotificationType notificationType, String textSearch);
}
