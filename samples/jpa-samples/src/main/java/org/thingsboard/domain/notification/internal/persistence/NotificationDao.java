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


import org.thingsboard.data.dao.Dao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.notification.Notification;
import org.thingsboard.domain.notification.NotificationStatus;
import org.thingsboard.domain.notification.internal.template.NotificationDeliveryMethod;

public interface NotificationDao extends Dao<Notification, Long> {
	boolean updateStatusByIdAndRecipientId(Long recipientId, Long notificationId, NotificationStatus status);

	boolean deleteByIdAndRecipientId(Long recipientId, Long notificationId);

	int updateStatusByDeliveryMethodAndRecipientId(NotificationDeliveryMethod deliveryMethod, Long recipientId, NotificationStatus notificationStatus);

	PageData<Notification> findUnreadByDeliveryMethodAndRecipientId(NotificationDeliveryMethod deliveryMethod, Long recipientId, PageLink pageLink);

	PageData<Notification> findByDeliveryMethodAndRecipientId( NotificationDeliveryMethod deliveryMethod, Long recipientId, PageLink pageLink);
}
