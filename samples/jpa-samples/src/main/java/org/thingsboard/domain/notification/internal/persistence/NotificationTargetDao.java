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
package org.thingsboard.domain.notification.internal.persistence;

import java.util.List;
import java.util.Set;
import org.thingsboard.data.dao.Dao;
import org.thingsboard.data.dao.TenantEntityDao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.notification.internal.targets.NotificationTarget;
import org.thingsboard.domain.notification.internal.targets.NotificationTargetType;
import org.thingsboard.domain.notification.internal.targets.UserFilterType;
import org.thingsboard.domain.notification.internal.template.NotificationType;

public interface NotificationTargetDao extends Dao<NotificationTarget, Long>, TenantEntityDao {
	List<NotificationTarget> findByTenantIdAndIds(String tenantId, Set<Long> ids);

	List<NotificationTarget> findByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType);

	PageData<NotificationTarget> findByTenantIdAndSupportedNotificationType(String tenantId, NotificationTargetType notificationTargetType, PageLink pageLink);

	void removeByTenantId(String tenantId);

	PageData<NotificationTarget> findNotificationTargetsByTenantId(String tenantId, NotificationType notificationType, PageLink pageLink);
}
