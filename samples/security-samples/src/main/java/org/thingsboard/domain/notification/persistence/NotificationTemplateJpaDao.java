package org.thingsboard.domain.notification.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.JpaAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.domain.notification.template.NotificationTemplate;
import org.thingsboard.domain.notification.template.NotificationType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class NotificationTemplateJpaDao extends JpaAbstractDao<NotificationTemplateEntity, NotificationTemplate, Long> implements NotificationTemplateDao {
	private final NotificationTemplateRepository repository;

	@Override
	protected Class<NotificationTemplateEntity> getEntityClass() {
		return NotificationTemplateEntity.class;
	}

	@Override
	protected JpaRepository<NotificationTemplateEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public Page<NotificationTemplate> findByTenantIdAndTemplateTypes(Pageable pageable, String tenantId, List<NotificationType> templateTypes) {

		return DaoUtil.toPage(repository.findByTenantIdAndNotificationTypesAndSearchText(pageable, tenantId, templateTypes, null));
	}

	@Override
	public void removeByTenantId(String tenantId) {
		repository.deleteByTenantId(tenantId);
	}
}
