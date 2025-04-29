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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import static org.thingsboard.common.ControllerConstants.DEVICE_PROFILE_ID;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.BaseController;
import static org.thingsboard.common.validation.Validator.checkNotNull;
import static org.thingsboard.server.security.SecurityUtils.getTenantId;
import org.thingsboard.server.security.permission.Operation;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DeviceProfileController extends BaseController {
	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/deviceProfile/{deviceProfileId}", method = RequestMethod.GET)
	public DeviceProfile getDeviceProfileById(@PathVariable(DEVICE_PROFILE_ID) Long deviceProfileId) throws ThingsboardException {
		return checkDeviceProfileId(deviceProfileId, Operation.READ);
	}

	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
	@RequestMapping(value = "/deviceProfileInfo/{deviceProfileId}", method = RequestMethod.GET)
	public DeviceProfileInfo getDeviceProfileInfoById(@PathVariable(DEVICE_PROFILE_ID) Long deviceProfileId) throws ThingsboardException {
		return new DeviceProfileInfo(checkDeviceProfileId(deviceProfileId, Operation.READ));
	}

	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
	@RequestMapping(value = "/deviceProfileInfo/default", method = RequestMethod.GET)
	public DeviceProfileInfo getDefaultDeviceProfileInfo() throws ThingsboardException {
		return checkNotNull(deviceProfileService.findDefaultDeviceProfileInfo(getTenantId()));
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/deviceProfile", method = RequestMethod.POST)
	@ResponseBody
	public DeviceProfile saveDeviceProfile(
		@RequestBody DeviceProfile deviceProfile) throws Exception {
		deviceProfile.setTenantId(getTenantId());

		DeviceProfile old = checkDeviceProfileId(deviceProfile.getId(), Operation.WRITE);
		return doSaveAndLog(deviceProfile, old, EntityType.DEVICE_PROFILE, (t) -> deviceProfileService.saveDeviceProfile(deviceProfile));
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/deviceProfile/{deviceProfileId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteDeviceProfile(
		@PathVariable(DEVICE_PROFILE_ID) Long deviceProfileId) throws ThingsboardException {
		deviceProfileService.deleteDeviceProfile(deviceProfileId);
	}

	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/deviceProfile/{deviceProfileId}/default", method = RequestMethod.POST)
	@ResponseBody
	public DeviceProfile setDefaultDeviceProfile(
		@PathVariable(DEVICE_PROFILE_ID) Long deviceProfileId) throws ThingsboardException {
		return deviceProfileService.setDefaultDeviceProfile(getTenantId(), deviceProfileId);
	}
}
