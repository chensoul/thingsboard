package org.thingsboard.domain.notification.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.notification.NotificationRequest;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class NotificationRequestServiceImpl implements NotificationRequestService {
	private final NotificationRequestDao notificationRequestDao;

	@Override
	public NotificationRequest saveNotificationRequest(NotificationRequest notificationRequest) {
		return notificationRequestDao.save(notificationRequest);
	}

	@Override
	public NotificationRequest findNotificationRequestById(Long id) {
		return notificationRequestDao.findById(id);
	}

	@Override
	public NotificationRequestInfo findNotificationRequestInfoById(Long id) {
		NotificationRequest request = notificationRequestDao.findById(id);
		NotificationRequestInfo requestInfo = new NotificationRequestInfo();

		return requestInfo;
	}

	@Override
	public Page<NotificationRequestInfo> findNotificationRequestsInfosByTenantIdAndOriginatorType(String tenantId, EntityType originatorType) {
		return null;
	}

	@Override
	public void deleteNotificationRequest(Long id) {
		notificationRequestDao.removeById(id);
	}
}
