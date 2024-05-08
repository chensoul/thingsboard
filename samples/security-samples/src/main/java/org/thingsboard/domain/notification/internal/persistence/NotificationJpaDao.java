package org.thingsboard.domain.notification.internal.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
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
@SqlDao
@RequiredArgsConstructor
@Component
public class NotificationJpaDao extends JpaAbstractDao<NotificationEntity, Notification, Long> implements NotificationDao {
	private final NotificationRepository repository;

	@Override
	protected Class<NotificationEntity> getEntityClass() {
		return NotificationEntity.class;
	}

	@Override
	protected JpaRepository<NotificationEntity, Long> getRepository() {
		return repository;
	}


	@Override
	public boolean updateStatusByIdAndRecipientId(Long recipientId, Long notificationId, NotificationStatus status) {
		return repository.updateStatusByIdAndRecipientId(notificationId, recipientId, status) != 0;
	}

	@Override
	public boolean deleteByIdAndRecipientId(Long recipientId, Long notificationId) {
		return repository.deleteByIdAndRecipientId(notificationId, recipientId) != 0;
	}

	@Override
	public int updateStatusByDeliveryMethodAndRecipientId(NotificationDeliveryMethod deliveryMethod, Long recipientId, NotificationStatus notificationStatus) {
		return updateStatusByDeliveryMethodAndRecipientId(deliveryMethod, recipientId, notificationStatus);
	}

	@Override
	public PageData<Notification> findUnreadByDeliveryMethodAndRecipientId(NotificationDeliveryMethod deliveryMethod, Long recipientId, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByDeliveryMethodAndRecipientIdAndStatusNot(deliveryMethod, recipientId, NotificationStatus.READ, pageLink.getTextSearch(),
			DaoUtil.toPageable(pageLink)));
	}

	@Override
	public PageData<Notification> findByDeliveryMethodAndRecipientId(NotificationDeliveryMethod deliveryMethod, Long recipientId, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByDeliveryMethodAndRecipientId(deliveryMethod, recipientId, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}
}
