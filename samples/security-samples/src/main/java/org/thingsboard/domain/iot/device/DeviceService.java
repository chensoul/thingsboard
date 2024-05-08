package org.thingsboard.domain.iot.device;

import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.user.User;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface DeviceService {
	Device findDeviceById(String deviceId);

	Device saveDeviceWithAccessToken(Device device, String accessToken);

	Device saveDeviceWithCredential(Device device, DeviceCredential deviceCredentials, User user) throws ThingsboardException;

	Device assignDeviceToMerchant(String deviceId, Long merchantId);

	Device unassignDeviceFromCustomer(String deviceId);

	void deleteDevice(String deviceId);

	Device assignDeviceToPublicMerchant(String deviceId);

	DeviceCredential getDeviceCredentialByDeviceId(String deviceId);

	DeviceCredential updateDeviceCredential(DeviceCredential deviceCredential);

	Device findDeviceByTenantIdAndName(String tenantId, String deviceName);
}
