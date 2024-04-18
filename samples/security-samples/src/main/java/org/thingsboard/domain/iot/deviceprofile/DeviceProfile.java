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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.model.BaseDataWithExtra;
import org.thingsboard.common.model.HasImage;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;
import org.thingsboard.domain.iot.HasOtaPackage;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;

@Data
@ToString(exclude = {"image"})
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class DeviceProfile extends BaseDataWithExtra<Long> implements HasName, HasTenantId, HasOtaPackage, HasImage {

	private static final long serialVersionUID = 6998485460273302018L;

	@NotBlank(message = "Tenant id should be specified")
	private String tenantId;

	@NoXss
	@Length
	@NotBlank(message = "Device profile name should be specified")
	private String name;

	@NoXss
	private String description;

	private String image;

	private boolean defaulted;

	@NotNull(message = "Device profile type should be specified")
	private DeviceProfileType type;

	@NotNull(message = "Device profile transport type should be specified")
	private DeviceTransportType transportType;

	private DeviceProfileProvisionType provisionType;

	@NoXss
	private String provisionDeviceKey;

	private Long defaultRuleChainId;

	private String defaultQueueName;

	private String firmwareId;
	private String softwareId;
}
