package org.thingsboard.domain.notification.internal.persistence;

import java.io.Serializable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
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
	public PageData<NotificationRequestInfo> findNotificationRequestsInfosByTenantIdAndOriginatorType(String tenantId, EntityType originatorType, PageLink pageLink) {
		return null;
	}

	@Override
	public List<NotificationRequest> findNotificationRequestsByRuleIdAndOriginatorEntityId(Long ruleId, Serializable originatorEntityId) {
		return List.of();
	}

	@Override
	public void deleteNotificationRequest(Long id) {
		notificationRequestDao.removeById(id);
	}
}
