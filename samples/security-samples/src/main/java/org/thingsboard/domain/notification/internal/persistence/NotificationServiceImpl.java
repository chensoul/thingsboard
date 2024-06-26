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
