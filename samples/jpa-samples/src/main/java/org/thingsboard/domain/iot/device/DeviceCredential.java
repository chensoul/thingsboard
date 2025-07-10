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

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.domain.iot.device.credential.DeviceCredentialFilter;
import org.thingsboard.domain.iot.device.model.DeviceCredentialType;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceCredential extends BaseData<Long> implements DeviceCredentialFilter {

	private static final long serialVersionUID = -7869261127032877765L;
	private String deviceId;
	private DeviceCredentialType credentialType;
	private String credentialId;
	private String credentialValue;
}
