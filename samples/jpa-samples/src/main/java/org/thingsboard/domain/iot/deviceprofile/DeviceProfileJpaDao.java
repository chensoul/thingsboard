package org.thingsboard.domain.iot.deviceprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
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
public class DeviceProfileJpaDao extends JpaAbstractDao<DeviceProfileEntity, DeviceProfile, Long> implements DeviceProfileDao {
	private final DeviceProfileRepository repository;

	@Override
	protected Class<DeviceProfileEntity> getEntityClass() {
		return DeviceProfileEntity.class;
	}

	@Override
	protected JpaRepository<DeviceProfileEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public DeviceProfileInfo findDeviceProfileInfoById(Long deviceProfileId) {
		return new DeviceProfileInfo(DaoUtil.getData(repository.findById(deviceProfileId)));
	}

	@Override
	public DeviceProfile findDefaultDeviceProfile(String tenantId) {
		return null;
	}

	@Override
	public DeviceProfile findByName(String tenantId, String profileName) {
		return null;
	}
}
