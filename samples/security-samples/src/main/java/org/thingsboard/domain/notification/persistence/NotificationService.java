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


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thingsboard.domain.notification.Notification;
import org.thingsboard.domain.notification.NotificationStatus;

public interface NotificationService {

	Notification saveNotification(Notification notification);

	Notification findNotificationById(Long notificationId);

	int markNotificationAsRead(Long recipientId, Long notificationId);

	int markAllNotificationsAsRead(Long recipientId);

	Page<Notification> findNotificationsByRecipientIdAndReadStatus(Pageable pageable, Long recipientId, NotificationStatus status, Integer limit);

	int countUnreadNotificationsByRecipientId(Long recipientId);

	boolean deleteNotification(Long recipientId, Long notificationId);

}
