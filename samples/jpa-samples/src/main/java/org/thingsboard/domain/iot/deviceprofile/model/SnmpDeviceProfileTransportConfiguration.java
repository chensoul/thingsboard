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
///**
// * Copyright © 2016-2024 The Thingsboard Authors
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.thingsboard.domain.iot.deviceprofile;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import java.util.List;
//import lombok.Data;
//import org.thingsboard.domain.iot.device.DeviceTransportType;
//
//@Data
//public class SnmpDeviceProfileTransportConfiguration implements DeviceProfileTransportConfiguration {
//	private Integer timeoutMs;
//	private Integer retries;
//	private List<SnmpCommunicationConfig> communicationConfigs;
//
//	@Override
//	public DeviceTransportType getType() {
//		return DeviceTransportType.SNMP;
//	}
//
//	@Override
//	public void validate() {
//		if (!isValid()) {
//			throw new IllegalArgumentException("SNMP transport configuration is not valid");
//		}
//	}
//
//	@JsonIgnore
//	private boolean isValid() {
//		return timeoutMs != null && timeoutMs >= 0 && retries != null && retries >= 0
//			   && communicationConfigs != null
//			   && communicationConfigs.stream().allMatch(config -> config != null && config.isValid());
//	}
//
//}
