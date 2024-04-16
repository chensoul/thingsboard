package org.thingsboard.domain.notification.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
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
public class MybatisNotificationTargetDao extends MybatisAbstractDao<NotificationTargetEntity, NotificationTarget> implements NotificationTargetDao {
	private NotificationTargetMapper mapper;

	@Override
	protected Class<NotificationTargetEntity> getEntityClass() {
		return NotificationTargetEntity.class;
	}

	@Override
	protected BaseMapper<NotificationTargetEntity> getRepository() {
		return mapper;
	}

	@Override
	public List<NotificationTarget> findByTenantIdAndIds(String tenantId, Set<Long> ids) {
		List<NotificationTargetEntity> list = mapper.selectList(Wrappers.<NotificationTargetEntity>lambdaQuery().eq(NotificationTargetEntity::getTenantId, tenantId).in(NotificationTargetEntity::getId, ids));
		return DaoUtil.convertDataList(list);
	}

	@Override
	public List<NotificationTarget> findByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType) {
		return List.of();
	}

	@Override
	public Page<NotificationTarget> findByTenantIdAndSupportedNotificationType(Pageable pageable, String tenantId, NotificationTargetType notificationTargetType) {
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<NotificationTargetEntity> pageResult = mapper.selectPage(toMybatisPage(pageable),
			Wrappers.<NotificationTargetEntity>lambdaQuery()
				.eq(StringUtils.isNotEmpty(tenantId), NotificationTargetEntity::getTenantId, tenantId)
				.like(notificationTargetType != null, NotificationTargetEntity::getConfig, notificationTargetType));

		return DaoUtil.toPageData(pageResult);
	}

	@Override
	public void removeByTenantId(String tenantId) {
		mapper.delete(Wrappers.<NotificationTargetEntity>lambdaQuery().eq(NotificationTargetEntity::getTenantId, tenantId));
	}

	@Override
	public Page<NotificationTarget> findNotificationTargetsByTenantId(Pageable pageable, String tenantId, NotificationType notificationType, String textSearch) {
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<NotificationTargetEntity> pageResult = mapper.selectPage(toMybatisPage(pageable),
			Wrappers.<NotificationTargetEntity>lambdaQuery()
				.eq(StringUtils.isNotEmpty(tenantId), NotificationTargetEntity::getTenantId, tenantId)
				.like(notificationType != null, NotificationTargetEntity::getConfig, notificationType)
				.or(StringUtils.isNotBlank(textSearch), i -> i.like(NotificationTargetEntity::getName, textSearch).or().like(NotificationTargetEntity::getDescription, textSearch)));

		return DaoUtil.toPageData(pageResult);
	}

	@Override
	public Long countByTenantId(String tenantId) {
		return mapper.selectCount(Wrappers.<NotificationTargetEntity>lambdaQuery().eq(NotificationTargetEntity::getTenantId, tenantId));
	}
}
