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

import java.util.Objects;
import java.util.Set;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.common.validation.NoXss;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;

@Data
public class MqttDeviceProfileTransportConfiguration implements DeviceProfileTransportConfiguration {

	@NoXss
	private String deviceTelemetryTopic = MqttTopics.DEVICE_TELEMETRY_TOPIC;
	@NoXss
	private String deviceAttributesTopic = MqttTopics.DEVICE_ATTRIBUTES_TOPIC;
	@NoXss
	private String deviceAttributesSubscribeTopic = MqttTopics.DEVICE_ATTRIBUTES_TOPIC;

	private TransportPayloadTypeConfiguration transportPayloadTypeConfiguration;
	private boolean sparkplug;
	private Set<String> sparkplugAttributesMetricNames;
	private boolean sendAckOnValidationException;

	@Override
	public DeviceTransportType getType() {
		return DeviceTransportType.MQTT;
	}

	public TransportPayloadTypeConfiguration getTransportPayloadTypeConfiguration() {
		return Objects.requireNonNullElseGet(transportPayloadTypeConfiguration, JsonTransportPayloadConfiguration::new);
	}

	public String getDeviceTelemetryTopic() {
		return StringUtils.defaultString(deviceTelemetryTopic, MqttTopics.DEVICE_TELEMETRY_TOPIC);
	}

	public String getDeviceAttributesTopic() {
		return StringUtils.defaultString(deviceAttributesTopic, MqttTopics.DEVICE_ATTRIBUTES_TOPIC);
	}

	public String getDeviceAttributesSubscribeTopic() {
		return StringUtils.defaultString(deviceAttributesSubscribeTopic, MqttTopics.DEVICE_ATTRIBUTES_TOPIC);
	}

}
