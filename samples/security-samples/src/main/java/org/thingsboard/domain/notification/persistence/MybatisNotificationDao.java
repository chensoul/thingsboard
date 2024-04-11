package org.thingsboard.domain.notification.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.domain.notification.Notification;
import org.thingsboard.domain.notification.NotificationStatus;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@AllArgsConstructor
@Component
public class MybatisNotificationDao extends MybatisAbstractDao<NotificationEntity, Notification> implements NotificationDao {
	private NotificationMapper mapper;

	@Override
	protected Class<NotificationEntity> getEntityClass() {
		return NotificationEntity.class;
	}

	@Override
	protected BaseMapper<NotificationEntity> getRepository() {
		return mapper;
	}


	@Override
	public Page<Notification> findByRecipientIdAndStatus(Pageable pageable, Long recipientId, NotificationStatus status, Integer limit) {
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<NotificationEntity> pageResult = mapper.selectPage(toMybatisPage(pageable),
			Wrappers.<NotificationEntity>lambdaQuery()
				.eq(NotificationEntity::getRecipientId, recipientId)
				.eq(status != null, NotificationEntity::getStatus, status)
				.last(limit != null, "limit " + limit)
		);

		return DaoUtil.toPageData(pageResult);
	}

	@Override
	public int updateStatusByRecipientId(Long recipientId, NotificationStatus status) {
		return mapper.update(Wrappers.<NotificationEntity>lambdaUpdate().eq(NotificationEntity::getRecipientId, recipientId).set(NotificationEntity::getStatus, status));
	}

	@Override
	public int updateStatusByIdAndRecipientId(Long recipientId, Long notificationId, NotificationStatus status) {
		return mapper.update(Wrappers.<NotificationEntity>lambdaUpdate()
			.eq(NotificationEntity::getRecipientId, recipientId).eq(NotificationEntity::getId, notificationId)
			.set(NotificationEntity::getStatus, status));
	}

	@Override
	public boolean deleteByIdAndRecipientId(Long recipientId, Long notificationId) {
		return mapper.delete(Wrappers.<NotificationEntity>lambdaQuery().eq(NotificationEntity::getRecipientId, recipientId).eq(NotificationEntity::getId, notificationId)) > 0;
	}
}
