package org.thingsboard.domain.iot.deviceprofile;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MybatisDeviceProfileDao extends MybatisAbstractDao<DeviceProfileEntity, DeviceProfile> implements DeviceProfileDao {
	private final DeviceProfileMapper mapper;

	@Override
	protected Class<DeviceProfileEntity> getEntityClass() {
		return DeviceProfileEntity.class;
	}

	@Override
	protected BaseMapper<DeviceProfileEntity> getRepository() {
		return mapper;
	}

	@Override
	public DeviceProfileInfo findDeviceProfileInfoById(Long deviceProfileId) {
		return new DeviceProfileInfo(DaoUtil.getData(mapper.selectById(deviceProfileId)));
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
