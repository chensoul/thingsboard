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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.notification.Notification;
import org.thingsboard.domain.notification.NotificationStatus;
import org.thingsboard.domain.notification.internal.template.NotificationDeliveryMethod;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
	private final NotificationDao notificationDao;

	@Override
	public Notification saveNotification(Notification notification) {
		return notificationDao.save(notification);
	}

	@Override
	public Notification findNotificationById(Long notificationId) {
		return notificationDao.findById(notificationId);
	}

	@Override
	public boolean markNotificationAsRead(Long recipientId, Long notificationId) {
		boolean updated = notificationDao.updateStatusByIdAndRecipientId(recipientId, notificationId, NotificationStatus.READ);

		if (updated) {
//			log.trace("Marked all notifications as read (recipient id: {}, tenant id: {})", recipientId, tenantId);
//			NotificationUpdate update = NotificationUpdate.builder()
//				.updated(true)
//				.allNotifications(true)
//				.newStatus(NotificationStatus.READ)
//				.build();
//			onNotificationUpdate(tenantId, recipientId, update);
		}
		return updated;
	}

	@Override
	public int markAllNotificationsAsRead(NotificationDeliveryMethod deliveryMethod, Long recipientId) {
		return notificationDao.updateStatusByDeliveryMethodAndRecipientId(deliveryMethod, recipientId, NotificationStatus.READ);
	}

	@Override
	public PageData<Notification> findNotificationsByRecipientIdAndReadStatus( NotificationDeliveryMethod deliveryMethod, Long recipientId, boolean unreadOnly, PageLink pageLink) {
		if (unreadOnly) {
			return notificationDao.findUnreadByDeliveryMethodAndRecipientId(deliveryMethod, recipientId,pageLink);
		} else {
			return notificationDao.findByDeliveryMethodAndRecipientId( deliveryMethod, recipientId,pageLink);
		}
	}

	@Override
	public int countUnreadNotificationsByRecipientId(Long recipientId) {
		return 0;
	}

	@Override
	public boolean deleteNotification(Long recipientId,Long notificationId) {
		return notificationDao.deleteByIdAndRecipientId(recipientId,notificationId);
	}

}
