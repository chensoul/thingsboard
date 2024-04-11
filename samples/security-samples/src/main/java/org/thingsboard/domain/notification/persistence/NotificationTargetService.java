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
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.notification.targets.NotificationTarget;
import org.thingsboard.domain.notification.targets.UserFilterType;
import org.thingsboard.domain.notification.template.NotificationType;
import org.thingsboard.domain.user.model.User;


public interface NotificationTargetService {
	NotificationTarget saveNotificationTarget(NotificationTarget notificationTarget) ;

	NotificationTarget findNotificationTargetById(Long id);

	Page<User> findRecipientsForNotificationTargetConfig(Pageable pageable, String tenantId, Long id) throws ThingsboardException;

	List<NotificationTarget> findNotificationTargetsByTenantIdAndIds(String tenantId, Set<Long> ids);

	List<NotificationTarget> findNotificationTargetsByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType);

	Page<NotificationTarget> findNotificationTargetsByTenantId(Pageable pageable, String tenantId, NotificationType notificationType, String textSearch);

	void deleteNotificationTargetById(Long id);
}
