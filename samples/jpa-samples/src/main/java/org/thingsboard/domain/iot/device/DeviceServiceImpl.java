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
package org.thingsboard.domain.iot.device;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.iot.device.model.CoapDeviceTransportConfiguration;
import org.thingsboard.domain.iot.device.model.DefaultDeviceConfiguration;
import org.thingsboard.domain.iot.device.model.DefaultDeviceTransportConfiguration;
import org.thingsboard.domain.iot.device.model.DeviceCredentialType;
import org.thingsboard.domain.iot.device.model.DeviceData;
import org.thingsboard.domain.iot.device.model.Lwm2mDeviceTransportConfiguration;
import org.thingsboard.domain.iot.device.model.MqttDeviceTransportConfiguration;
import org.thingsboard.domain.iot.device.model.SnmpDeviceTransportConfiguration;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfile;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfileService;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfileType;
import org.thingsboard.domain.user.User;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class DeviceServiceImpl implements DeviceService {
	private final DeviceDao deviceDao;
	private final DeviceValidator deviceValidator;
	private final DeviceProfileService deviceProfileService;
	private final DeviceCredentialService deviceCredentialService;

	@Override
	public Device findDeviceById(String deviceId) {
		return deviceDao.findById(deviceId);
	}

	@Override
	public Device saveDeviceWithAccessToken(Device device, String accessToken) {
		return null;
	}

	@Override
	public Device saveDeviceWithCredential(Device device, DeviceCredential deviceCredentials, User user) throws ThingsboardException {
		return null;
	}

	@Override
	public Device assignDeviceToMerchant(String deviceId, Long merchantId) {
		return null;
	}

	@Override
	public Device unassignDeviceFromCustomer(String deviceId) {
		return null;
	}

	@Override
	public void deleteDevice(String deviceId) {

	}

	@Override
	public Device assignDeviceToPublicMerchant(String deviceId) {
		return null;
	}

	@Override
	public DeviceCredential getDeviceCredentialByDeviceId(String deviceId) {
		return null;
	}

	@Override
	public DeviceCredential updateDeviceCredential(DeviceCredential deviceCredential) {
		return null;
	}

	@Override
	public Device findDeviceByTenantIdAndName(String tenantId, String deviceName) {
		return null;
	}

	private Device saveDeviceWithoutCredential(Device device, boolean doValidate) {
		Device oldDevice = null;
		if (doValidate) {
			oldDevice = deviceValidator.validate(device);
		} else if (device.getId() != null) {
			oldDevice = findDeviceById(device.getId());
		}
		try {
			DeviceProfile deviceProfile;
			if (device.getDeviceProfileId() == null) {
				if (!StringUtils.isEmpty(device.getType())) {
					deviceProfile = this.deviceProfileService.findOrCreateDeviceProfile(device.getTenantId(), device.getType());
				} else {
					deviceProfile = this.deviceProfileService.findDefaultDeviceProfile(device.getTenantId());
				}
				device.setDeviceProfileId(deviceProfile.getId());
			} else {
				deviceProfile = this.deviceProfileService.findDeviceProfileById(device.getDeviceProfileId());
				if (deviceProfile == null) {
					throw new DataValidationException("Device is referencing non existing device profile!");
				}
				if (!deviceProfile.getTenantId().equals(device.getTenantId())) {
					throw new DataValidationException("Device can`t be referencing to device profile from different tenant!");
				}
			}
			device.setType(deviceProfile.getName());
			device.setDeviceData(JacksonUtil.readTree(syncDeviceData(deviceProfile, device.getDeviceData())));
			Device savedDevice = deviceDao.save(device);
			return savedDevice;
		} catch (Exception t) {
			throw t;
		}
	}

	private DeviceData syncDeviceData(DeviceProfile deviceProfile, JsonNode jsonNode) {
		DeviceData deviceData = null;
		if (jsonNode == null) {
			deviceData = new DeviceData();
		} else {
			deviceData = JacksonUtil.convertValue(jsonNode, DeviceData.class);
		}
		if (deviceData.getConfiguration() == null || !deviceProfile.getType().equals(deviceData.getConfiguration().getType())) {
			if (deviceProfile.getType() == DeviceProfileType.DEFAULT) {
				deviceData.setConfiguration(new DefaultDeviceConfiguration());
			}
		}
		if (deviceData.getTransportConfiguration() == null || !deviceProfile.getTransportType().equals(deviceData.getTransportConfiguration().getType())) {
			switch (deviceProfile.getTransportType()) {
				case DEFAULT:
					deviceData.setTransportConfiguration(new DefaultDeviceTransportConfiguration());
					break;
				case MQTT:
					deviceData.setTransportConfiguration(new MqttDeviceTransportConfiguration());
					break;
				case COAP:
					deviceData.setTransportConfiguration(new CoapDeviceTransportConfiguration());
					break;
				case LWM2M:
					deviceData.setTransportConfiguration(new Lwm2mDeviceTransportConfiguration());
					break;
				case SNMP:
					deviceData.setTransportConfiguration(new SnmpDeviceTransportConfiguration());
					break;
			}
		}
		return deviceData;
	}

	private Device doSaveDevice(Device device, String accessToken, boolean doValidate) {
		Device savedDevice = this.saveDeviceWithoutCredential(device, doValidate);
		if (device.getId() == null) {
			DeviceCredential deviceCredentials = new DeviceCredential();
			deviceCredentials.setDeviceId(savedDevice.getId());
			deviceCredentials.setCredentialType(DeviceCredentialType.ACCESS_TOKEN);
			deviceCredentials.setCredentialId(!StringUtils.isEmpty(accessToken) ? accessToken : RandomStringUtils.randomAlphanumeric(20));
			deviceCredentialService.createDeviceCredential(savedDevice.getTenantId(), deviceCredentials);
		}
		return savedDevice;
	}
}
