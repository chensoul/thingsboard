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
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.notification.internal.targets.NotificationTarget;
import org.thingsboard.domain.notification.internal.targets.UserFilterType;
import org.thingsboard.domain.notification.internal.template.NotificationType;
import org.thingsboard.domain.user.User;


public interface NotificationTargetService {
	NotificationTarget saveNotificationTarget(NotificationTarget notificationTarget) ;

	NotificationTarget findNotificationTargetById(Long id);

	PageData<User> findRecipientsForNotificationTargetConfig(String tenantId, Long id, PageLink pageLink) throws ThingsboardException;

	List<NotificationTarget> findNotificationTargetsByTenantIdAndIds(String tenantId, Set<Long> ids);

	List<NotificationTarget> findNotificationTargetsByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType);

	PageData<NotificationTarget> findNotificationTargetsByTenantId(String tenantId, NotificationType notificationType, PageLink pageLink);

	void deleteNotificationTargetById(Long id);
}
