package org.thingsboard.domain.iot.device;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
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
public class MybatisDeviceDao extends AbstractMybatisDao<DeviceEntity, Device> implements DeviceDao {
	private final DeviceMapper mapper;

	@Override
	protected Class<DeviceEntity> getEntityClass() {
		return DeviceEntity.class;
	}

	@Override
	protected BaseMapper<DeviceEntity> getRepository() {
		return mapper;
	}
}
