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
package org.thingsboard.domain.iot.device.credential;

import org.thingsboard.domain.iot.device.model.DeviceCredentialType;

public class DeviceTokenCredential implements DeviceCredentialFilter {

    private final String token;

	public DeviceTokenCredential(String token) {
        this.token = token;
    }

    @Override
	public DeviceCredentialType getCredentialType() {
		return DeviceCredentialType.ACCESS_TOKEN;
    }

    @Override
	public String getCredentialId() {
        return token;
    }

    @Override
    public String toString() {
        return "DeviceTokenCredentials [token=" + token + "]";
    }

}
