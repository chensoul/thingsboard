package org.thingsboard.domain.notification.persistence;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.jpa.JpaAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.page.PageData;
import org.thingsboard.common.dao.page.PageLink;
import org.thingsboard.domain.notification.targets.NotificationTarget;
import org.thingsboard.domain.notification.targets.NotificationTargetType;
import org.thingsboard.domain.notification.targets.UserFilterType;
import org.thingsboard.domain.notification.template.NotificationType;

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
