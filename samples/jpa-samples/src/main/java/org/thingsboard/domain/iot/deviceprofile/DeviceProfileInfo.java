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
package org.thingsboard.domain.iot.deviceprofile;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.thingsboard.common.model.EntityInfo;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "image")
public class DeviceProfileInfo extends EntityInfo {

	private String image;
	private DeviceProfileType type;
	private DeviceTransportType transportType;
	private String tenantId;

	public DeviceProfileInfo(Serializable id, String name, String image, DeviceProfileType type, DeviceTransportType transportType, String tenantId) {
		super(id, name);
		this.image = image;
		this.type = type;
		this.transportType = transportType;
		this.tenantId = tenantId;
	}

	public DeviceProfileInfo(DeviceProfile profile) {
		this(profile.getId(), profile.getName(), profile.getImage(),
			profile.getType(), profile.getTransportType(), profile.getTenantId());
	}

}
