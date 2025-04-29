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

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.data.dao.jpa.JsonConverter;
import org.thingsboard.data.dao.jpa.LongBaseEntity;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
@Entity
@Table(name = "device_profile")
@EqualsAndHashCode(callSuper = true)
public class DeviceProfileEntity extends LongBaseEntity<DeviceProfile> {
	private String tenantId;

	private String name;

	@Enumerated(EnumType.STRING)
	private DeviceProfileType type;

	private String image;

	@Enumerated(EnumType.STRING)
	private DeviceTransportType transportType;

	@Enumerated(EnumType.STRING)
	private DeviceProfileProvisionType provisionType;

	private String description;

	private boolean defaulted;

	private Long defaultRuleChainId;

	private String defaultQueueName;

	@Convert(converter = JsonConverter.class)
	private JsonNode extra;

	private String provisionDeviceKey;

	private Long firmwareId;

	private Long softwareId;

	@Override
	public DeviceProfile toData() {
		return null;
	}
}
