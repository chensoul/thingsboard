package org.thingsboard.domain.notification.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
import org.thingsboard.domain.notification.NotificationRequest;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MybatisNotificationRequestDao extends AbstractMybatisDao<NotificationRequestEntity, NotificationRequest> implements NotificationRequestDao {
	private final NotificationRequestMapper mapper;

	@Override
	protected Class<NotificationRequestEntity> getEntityClass() {
		return NotificationRequestEntity.class;
	}

	@Override
	protected BaseMapper<NotificationRequestEntity> getRepository() {
		return mapper;
	}
}
