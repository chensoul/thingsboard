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
package org.thingsboard.domain.iot.deviceprofile.model;

import lombok.Data;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;
import org.thingsboard.domain.iot.device.model.PowerSavingConfiguration;

@Data
public class CoapDeviceProfileTransportConfiguration implements DeviceProfileTransportConfiguration {

	private CoapDeviceTypeConfiguration coapDeviceTypeConfiguration;
	private PowerSavingConfiguration clientSettings;

	@Override
	public DeviceTransportType getType() {
		return DeviceTransportType.COAP;
	}

	public CoapDeviceTypeConfiguration getCoapDeviceTypeConfiguration() {
		if (coapDeviceTypeConfiguration != null) {
			return coapDeviceTypeConfiguration;
		} else {
			return new DefaultCoapDeviceTypeConfiguration();
		}
	}
}
