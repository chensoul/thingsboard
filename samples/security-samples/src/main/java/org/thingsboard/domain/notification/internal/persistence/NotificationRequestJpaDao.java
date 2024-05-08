package org.thingsboard.domain.notification.internal.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
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
public class NotificationRequestJpaDao extends JpaAbstractDao<NotificationRequestEntity, NotificationRequest, Long> implements NotificationRequestDao {
	private final NotificationRequestRepository repository;

	@Override
	protected Class<NotificationRequestEntity> getEntityClass() {
		return NotificationRequestEntity.class;
	}

	@Override
	protected JpaRepository<NotificationRequestEntity, Long> getRepository() {
		return repository;
	}
}
