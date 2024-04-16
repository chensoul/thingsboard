package org.thingsboard.domain.iot.deviceprofile;

import org.thingsboard.common.dao.Dao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface DeviceProfileDao extends Dao<DeviceProfile> {
	DeviceProfileInfo findDeviceProfileInfoById(Long deviceProfileId);

	DeviceProfile findDefaultDeviceProfile(String tenantId);

	DeviceProfile findByName(String tenantId, String profileName);
}
