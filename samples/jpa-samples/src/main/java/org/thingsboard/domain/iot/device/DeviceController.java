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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import static org.thingsboard.common.ControllerConstants.DEVICE_ID;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.BaseController;
import static org.thingsboard.common.validation.Validator.checkNotNull;
import org.thingsboard.domain.iot.device.model.SaveDeviceWithCredentialsRequest;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
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
public class DeviceController extends BaseController {
	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
	@RequestMapping(value = "/device/{deviceId}", method = RequestMethod.GET)
	public Device getDeviceById(@PathVariable(DEVICE_ID) String deviceId) throws ThingsboardException {
		return checkDeviceId(deviceId, Operation.READ);
	}

	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
	@RequestMapping(value = "/device", method = RequestMethod.POST)
	public Device saveDevice(@RequestBody Device device,
							 @RequestParam(name = "accessToken", required = false) String accessToken) throws Exception {
		device.setTenantId(getCurrentUser().getTenantId());
		if (device.getId() != null) {
			checkDeviceId(device.getId(), Operation.WRITE);
		} else {
			checkEntity(device, EntityType.DEVICE, Operation.CREATE);
		}
		return deviceService.saveDeviceWithAccessToken(device, accessToken);
	}

	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
	@RequestMapping(value = "/device-with-credentials", method = RequestMethod.POST)
	public Device saveDeviceWithCredentials(@Valid @RequestBody SaveDeviceWithCredentialsRequest deviceAndCredentials) throws ThingsboardException {
		Device device = deviceAndCredentials.getDevice();
		DeviceCredential credentials = deviceAndCredentials.getDeviceCredential();
		device.setTenantId(getCurrentUser().getTenantId());
//		checkEntity(device.getId(), device, EntityType.DEVICE, Operation.READ);
		return deviceService.saveDeviceWithCredential(device, credentials, getCurrentUser());
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/device/{deviceId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteDevice(@PathVariable(DEVICE_ID) String deviceId) throws Exception {
		deviceService.deleteDevice(deviceId);
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/customer/{customerId}/device/{deviceId}", method = RequestMethod.POST)
	@ResponseBody
	public Device assignDeviceToCustomer(@PathVariable("customerId") Long merchantId,
										 @PathVariable(DEVICE_ID) String deviceId) throws ThingsboardException {
		return deviceService.assignDeviceToMerchant(deviceId, merchantId);
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/customer/device/{deviceId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Device unassignDeviceFromCustomer(@PathVariable(DEVICE_ID) String deviceId) throws ThingsboardException {
		return deviceService.unassignDeviceFromCustomer(deviceId);
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/customer/public/device/{deviceId}", method = RequestMethod.POST)
	@ResponseBody
	public Device assignDeviceToPublicCustomer(@PathVariable(DEVICE_ID) String deviceId) throws ThingsboardException {
		return deviceService.assignDeviceToPublicMerchant(deviceId);
	}

	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
	@RequestMapping(value = "/device/{deviceId}/credentials", method = RequestMethod.GET)
	@ResponseBody
	public DeviceCredential getDeviceCredentialByDeviceId(@PathVariable(DEVICE_ID) String deviceId) throws ThingsboardException {
		return deviceService.getDeviceCredentialByDeviceId(deviceId);
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/device/credentials", method = RequestMethod.POST)
	@ResponseBody
	public DeviceCredential updateDeviceCredentials(
		@RequestBody DeviceCredential deviceCredential) throws ThingsboardException {
		return deviceService.updateDeviceCredential(deviceCredential);
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/tenant/devices", params = {"deviceName"}, method = RequestMethod.GET)
	@ResponseBody
	public Device getTenantDevice(
		@RequestParam String deviceName) throws ThingsboardException {
		return checkNotNull(deviceService.findDeviceByTenantIdAndName(getCurrentUser().getTenantId(), deviceName));
	}

}
