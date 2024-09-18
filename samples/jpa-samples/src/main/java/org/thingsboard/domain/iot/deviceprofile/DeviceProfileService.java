package org.thingsboard.domain.iot.deviceprofile;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface DeviceProfileService {
	DeviceProfile findDeviceProfileById(Long deviceProfileId);

	DeviceProfile findDeviceProfileByName(String tenantId, String profileName);

	DeviceProfileInfo findDefaultDeviceProfileInfo(String tenantId);

	DeviceProfile findDefaultDeviceProfile(String tenantId);

	DeviceProfile findOrCreateDeviceProfile(String tenantId, String profileName);

	DeviceProfile saveDeviceProfile(DeviceProfile deviceProfile);

	DeviceProfile saveDeviceProfile(DeviceProfile deviceProfile, boolean doValidate, boolean publishSaveEvent);

	DeviceProfile setDefaultDeviceProfile(String tenantId, Long deviceProfileId);

	void deleteDeviceProfile( Long deviceProfileId);
}
