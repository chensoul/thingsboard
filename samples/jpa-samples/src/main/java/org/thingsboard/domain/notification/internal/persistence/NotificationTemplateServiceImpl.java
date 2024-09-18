package org.thingsboard.domain.notification.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.domain.notification.internal.template.NotificationTemplate;
import org.thingsboard.domain.notification.internal.template.NotificationType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
	private final NotificationTemplateDao notificationTemplateDao;

	@Override
	public NotificationTemplate findNotificationTemplateById(Long id) {
		return notificationTemplateDao.findById(id);
	}

	@Override
	public NotificationTemplate saveNotificationTemplate(NotificationTemplate notificationTemplate) {
		return notificationTemplateDao.save(notificationTemplate);
	}

	@Override
	public Page<NotificationTemplate> findNotificationTemplatesByTenantIdAndTemplateTypes(Pageable pageable, String tenantId, List<NotificationType> notificationTypes) {
		return notificationTemplateDao.findByTenantIdAndTemplateTypes(pageable, tenantId, notificationTypes);
	}

	@Override
	public void deleteNotificationTemplateById(Long id) {
		notificationTemplateDao.removeById(id);
	}

	@Override
	public void deleteNotificationTemplatesByTenantId(String tenantId) {
		notificationTemplateDao.removeByTenantId(tenantId);
	}
}
