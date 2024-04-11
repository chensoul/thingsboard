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
package com.chensoul.netty.thingsboard.mqtt.session;

import com.chensoul.netty.thingsboard.mqtt.auth.DeviceAuthResult;
import com.chensoul.netty.thingsboard.mqtt.auth.DeviceAuthService;
import com.chensoul.netty.thingsboard.mqtt.auth.TransportDeviceInfo;
import com.chensoul.netty.thingsboard.mqtt.auth.ValidateDeviceCredentialsResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.domain.iot.device.Device;
import org.thingsboard.domain.iot.device.DeviceProfile;
import org.thingsboard.domain.iot.device.credential.DeviceTokenCredential;

/**
 * @author Andrew Shvayka
 */
@Slf4j
@Data
@RequiredArgsConstructor
public abstract class DeviceAwareSessionContext implements SessionContext {

	@Getter
	protected final UUID sessionId;
	@Getter
	private volatile String deviceId;
	@Getter
	protected volatile TransportDeviceInfo deviceInfo;
	@Getter
	@Setter
	protected volatile DeviceProfile deviceProfile;

	@Setter
	private volatile boolean connected;

	private DeviceAuthService authService;

	public DeviceAwareSessionContext(UUID sessionId, DeviceAuthService authService) {
		this.sessionId = sessionId;
		this.authService = authService;
	}

	public void setDeviceInfo(TransportDeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
		this.deviceId = deviceInfo.getDeviceId();
	}

	public ValidateDeviceCredentialsResponse login(DeviceTokenCredential credential) {
		DeviceAuthResult result = authService.process(credential);
		ValidateDeviceCredentialsResponse response = null;
		if (result.isSuccess()) {
			Optional<Device> deviceOpt = authService.findDeviceById(result.getDeviceId());
			if (deviceOpt.isPresent()) {
				Device device = deviceOpt.get();
				response = ValidateDeviceCredentialsResponse.builder().deviceInfo(new TransportDeviceInfo()).credential(credential.getCredentialId()).build();
			}
			return response;
		} else {
			log.debug("Can't find device using credentials [{}] due to {}", credential, result.getErrorMsg());
			return response;
		}

	}

//	@Override
//	public void onDeviceProfileUpdate(TransportProtos.SessionInfoProto sessionInfo, DeviceProfile deviceProfile) {
//		this.sessionInfo = sessionInfo;
//		this.deviceProfile = deviceProfile;
//		this.deviceInfo.setDeviceType(deviceProfile.getName());
//
//	}
//
//	@Override
//	public void onDeviceUpdate(TransportProtos.SessionInfoProto sessionInfo, Device device, Optional<DeviceProfile> deviceProfileOpt) {
//		this.sessionInfo = sessionInfo;
//		this.deviceInfo.setDeviceProfileId(device.getDeviceProfileId());
//		this.deviceInfo.setDeviceType(device.getType());
//		deviceProfileOpt.ifPresent(profile -> this.deviceProfile = profile);
//	}

	public boolean isConnected() {
		return connected;
	}

	public void setDisconnected() {
		this.connected = false;
	}


}
