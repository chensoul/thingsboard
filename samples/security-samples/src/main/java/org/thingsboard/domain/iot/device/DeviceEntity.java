/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.iot.device;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.data.dao.jpa.StringBaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "device")
public class DeviceEntity extends StringBaseEntity<Device> {

	private String tenantId;

	private Long merchantId;

	private String type;

	private String name;

	private String description;

	private String label;

	private Long deviceProfileId;

	private Long firmwareId;

	private Long softwareId;

	@Override
	public Device toData() {
		return null;
	}
}
