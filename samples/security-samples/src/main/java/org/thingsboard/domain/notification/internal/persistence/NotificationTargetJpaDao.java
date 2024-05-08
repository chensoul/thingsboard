package org.thingsboard.domain.notification.internal.persistence;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.notification.internal.targets.NotificationTarget;
import org.thingsboard.domain.notification.internal.targets.NotificationTargetType;
import org.thingsboard.domain.notification.internal.targets.UserFilterType;
import org.thingsboard.domain.notification.internal.template.NotificationType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@AllArgsConstructor
@Component
public class NotificationTargetJpaDao extends JpaAbstractDao<NotificationTargetEntity, NotificationTarget, Long> implements NotificationTargetDao {
	private NotificationTargetRepository repository;

	@Override
	protected Class<NotificationTargetEntity> getEntityClass() {
		return NotificationTargetEntity.class;
	}

	@Override
	protected JpaRepository<NotificationTargetEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public List<NotificationTarget> findByTenantIdAndIds(String tenantId, Set<Long> ids) {
		return DaoUtil.convertDataList(repository.findByTenantIdAndIdIn(tenantId, ids));
	}

	@Override
	public List<NotificationTarget> findByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType) {
		return List.of();
	}

	@Override
	public PageData<NotificationTarget> findByTenantIdAndSupportedNotificationType(String tenantId, NotificationTargetType notificationTargetType, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByTenantIdAndSearchTextAndUsersFilterTypeIfPresent(tenantId, List.of(notificationTargetType.name()), pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}

	@Override
	public void removeByTenantId(String tenantId) {
		repository.deleteByTenantId(tenantId);
	}

	@Override
	public PageData<NotificationTarget> findNotificationTargetsByTenantId(String tenantId, NotificationType notificationType, PageLink pageLink) {
		return DaoUtil.toPageData(null);
	}

	@Override
	public Long countByTenantId(String tenantId) {
		return repository.countByTenantId(tenantId);
	}
}
