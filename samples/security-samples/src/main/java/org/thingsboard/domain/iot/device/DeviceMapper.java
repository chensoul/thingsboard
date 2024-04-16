package org.thingsboard.domain.iot.device;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfileEntity;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface DeviceMapper extends BaseMapper<DeviceEntity> {
}
