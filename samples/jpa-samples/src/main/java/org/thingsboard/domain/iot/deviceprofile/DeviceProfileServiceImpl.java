/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.iot.deviceprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;
import org.thingsboard.domain.iot.deviceprofile.model.DefaultDeviceProfileConfiguration;
import org.thingsboard.domain.iot.deviceprofile.model.DefaultDeviceProfileTransportConfiguration;
import org.thingsboard.domain.iot.deviceprofile.model.DeviceProfileData;
import org.thingsboard.domain.iot.deviceprofile.model.DisabledDeviceProfileProvisionConfiguration;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class DeviceProfileServiceImpl implements DeviceProfileService {
	private static final String DEVICE_PROFILE_WITH_SUCH_NAME_ALREADY_EXISTS = "Device profile with such name already exists!";

	private final DeviceProfileDao deviceProfileDao;
	private final DeviceProfileValidator deviceProfileValidator;

	@Override
	public DeviceProfile findDeviceProfileById(Long id) {
		return deviceProfileDao.findById(id);
	}

	@Override
	public DeviceProfileInfo findDefaultDeviceProfileInfo(String tenantId) {
		return toDeviceProfileInfo(deviceProfileDao.findDefaultDeviceProfile(tenantId));
	}

	@Override
	public DeviceProfile findDefaultDeviceProfile(String tenantId) {
		return deviceProfileDao.findDefaultDeviceProfile(tenantId);
	}

	@Override
	public DeviceProfile findOrCreateDeviceProfile(String tenantId, String profileName) {
		DeviceProfile deviceProfile = findDeviceProfileByName(tenantId, profileName);
		if (deviceProfile == null) {
			try {
				deviceProfile = this.doCreateDefaultDeviceProfile(tenantId, profileName, profileName.equals("default"));
			} catch (DataValidationException e) {
				if (DEVICE_PROFILE_WITH_SUCH_NAME_ALREADY_EXISTS.equals(e.getMessage())) {
					deviceProfile = findDeviceProfileByName(tenantId, profileName);
				} else {
					throw e;
				}
			}
		}
		return deviceProfile;
	}

	private DeviceProfile doCreateDefaultDeviceProfile(String tenantId, String profileName, boolean defaultProfile) {
		DeviceProfile deviceProfile = new DeviceProfile();
		deviceProfile.setTenantId(tenantId);
		deviceProfile.setDefaulted(defaultProfile);
		deviceProfile.setName(profileName);
		deviceProfile.setType(DeviceProfileType.DEFAULT);
		deviceProfile.setTransportType(DeviceTransportType.DEFAULT);
		deviceProfile.setProvisionType(DeviceProfileProvisionType.DISABLED);
		deviceProfile.setDescription("Default device profile");
		DeviceProfileData deviceProfileData = new DeviceProfileData();
		DefaultDeviceProfileConfiguration configuration = new DefaultDeviceProfileConfiguration();
		DefaultDeviceProfileTransportConfiguration transportConfiguration = new DefaultDeviceProfileTransportConfiguration();
		DisabledDeviceProfileProvisionConfiguration provisionConfiguration = new DisabledDeviceProfileProvisionConfiguration(null);
		deviceProfileData.setConfiguration(configuration);
		deviceProfileData.setTransportConfiguration(transportConfiguration);
		deviceProfileData.setProvisionConfiguration(provisionConfiguration);
		deviceProfile.setExtra(JacksonUtil.readTree(deviceProfileData));
		return saveDeviceProfile(deviceProfile);
	}

	@Override
	public DeviceProfile findDeviceProfileByName(String tenantId, String profileName) {
		return deviceProfileDao.findByName(tenantId, profileName);
	}

	@Override
	public DeviceProfile saveDeviceProfile(DeviceProfile deviceProfile) {
		return saveDeviceProfile(deviceProfile, true, true);
	}

	@Override
	public DeviceProfile saveDeviceProfile(DeviceProfile deviceProfile, boolean doValidate, boolean publishSaveEvent) {
		DeviceProfile oldDeviceProfile = null;
		if (doValidate) {
			oldDeviceProfile = deviceProfileValidator.validate(deviceProfile);
		} else if (deviceProfile.getId() != null) {
			oldDeviceProfile = findDeviceProfileById(deviceProfile.getId());
		}
		DeviceProfile savedDeviceProfile;
		try {
			savedDeviceProfile = deviceProfileDao.save(deviceProfile);
//			publishEvictEvent(new DeviceProfileEvictEvent(savedDeviceProfile.getTenantId(), savedDeviceProfile.getName(),
//				oldDeviceProfile != null ? oldDeviceProfile.getName() : null, savedDeviceProfile.getId(), savedDeviceProfile.isDefault(),
//				oldDeviceProfile != null ? oldDeviceProfile.getProvisionDeviceKey() : null));
//			if (publishSaveEvent) {
//				eventPublisher.publishEvent(SaveEntityEvent.builder().tenantId(savedDeviceProfile.getTenantId()).entityId(savedDeviceProfile.getId())
//					.entity(savedDeviceProfile).oldEntity(oldDeviceProfile).created(oldDeviceProfile == null).build());
//			}
		} catch (Exception t) {
//			handleEvictEvent(new DeviceProfileEvictEvent(deviceProfile.getTenantId(), deviceProfile.getName(),
//				oldDeviceProfile != null ? oldDeviceProfile.getName() : null, null, deviceProfile.isDefault(),
//				oldDeviceProfile != null ? oldDeviceProfile.getProvisionDeviceKey() : null));
//			String unqProvisionKeyErrorMsg = DeviceProfileProvisionType.X509_CERTIFICATE_CHAIN.equals(deviceProfile.getProvisionType())
//				? "Device profile with such certificate already exists!"
//				: "Device profile with such provision device key already exists!";
//			checkConstraintViolation(t,
//				Map.of("device_profile_name_unq_key", DEVICE_PROFILE_WITH_SUCH_NAME_ALREADY_EXISTS,
//					"device_provision_key_unq_key", unqProvisionKeyErrorMsg,
//					"device_profile_external_id_unq_key", "Device profile with such external id already exists!"));
			throw t;
		}
//		if (oldDeviceProfile != null && !oldDeviceProfile.getName().equals(deviceProfile.getName())) {
//			PageLink pageLink = new PageLink(100);
//			PageData<Device> pageData;
//			do {
//				pageData = deviceDao.findDevicesByTenantIdAndProfileId(deviceProfile.getTenantId().getId(), deviceProfile.getUuidId(), pageLink);
//				for (Device device : pageData.getData()) {
//					device.setType(deviceProfile.getName());
//					deviceService.saveDevice(device);
//				}
//				pageLink = pageLink.nextPageLink();
//			} while (pageData.hasNext());
//		}
		return savedDeviceProfile;
	}

	@Override
	public DeviceProfile setDefaultDeviceProfile(String tenantId, Long deviceProfileId) {
		DeviceProfile deviceProfile = deviceProfileDao.findById(deviceProfileId);
		if (!deviceProfile.isDefaulted()) {
			deviceProfile.setDefaulted(true);
			DeviceProfile previousDefaultDeviceProfile = findDefaultDeviceProfile(tenantId);
			if (previousDefaultDeviceProfile == null) {
				return deviceProfileDao.save(deviceProfile);
			} else if (!previousDefaultDeviceProfile.getId().equals(deviceProfile.getId())) {
				previousDefaultDeviceProfile.setDefaulted(false);
				deviceProfileDao.save(previousDefaultDeviceProfile);
				return deviceProfileDao.save(deviceProfile);
			}
		}
		return deviceProfile;
	}

	@Override
	public void deleteDeviceProfile(Long deviceProfileId) {
		deviceProfileDao.removeById(deviceProfileId);
	}

	private DeviceProfileInfo toDeviceProfileInfo(DeviceProfile profile) {
		return profile == null ? null : new DeviceProfileInfo(profile.getId(), profile.getName(), profile.getImage(), profile.getType(), profile.getTransportType(), profile.getTenantId());
	}

}
