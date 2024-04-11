package org.thingsboard.domain.notification.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.domain.notification.Notification;
import org.thingsboard.domain.notification.NotificationStatus;

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
	public int markNotificationAsRead(Long recipientId, Long notificationId) {
		int updatedCount = notificationDao.updateStatusByIdAndRecipientId(recipientId, notificationId, NotificationStatus.READ);

		if (updatedCount > 0) {
//			log.trace("Marked all notifications as read (recipient id: {}, tenant id: {})", recipientId, tenantId);
//			NotificationUpdate update = NotificationUpdate.builder()
//				.updated(true)
//				.allNotifications(true)
//				.newStatus(NotificationStatus.READ)
//				.build();
//			onNotificationUpdate(tenantId, recipientId, update);
		}
		return updatedCount;
	}

	@Override
	public int markAllNotificationsAsRead(Long recipientId) {
		int updatedCount = notificationDao.updateStatusByRecipientId(recipientId, NotificationStatus.READ);

		if (updatedCount > 0) {
//			log.trace("Marked all notifications as read (recipient id: {}, tenant id: {})", recipientId, tenantId);
//			NotificationUpdate update = NotificationUpdate.builder()
//				.updated(true)
//				.allNotifications(true)
//				.newStatus(NotificationStatus.READ)
//				.build();
//			onNotificationUpdate(tenantId, recipientId, update);
		}
		return updatedCount;
	}

	@Override
	public Page<Notification> findNotificationsByRecipientIdAndReadStatus(Pageable pageable, Long recipientId, NotificationStatus status, Integer limit) {
		return notificationDao.findByRecipientIdAndStatus(pageable, recipientId, status, limit);
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
