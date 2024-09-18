package org.thingsboard.domain.iot.device;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class DeviceJpaDao extends JpaAbstractDao<DeviceEntity, Device, String> implements DeviceDao {
	private final DeviceRepository repository;

	@Override
	protected Class<DeviceEntity> getEntityClass() {
		return DeviceEntity.class;
	}

	@Override
	protected JpaRepository<DeviceEntity, String> getRepository() {
		return repository;
	}
}
