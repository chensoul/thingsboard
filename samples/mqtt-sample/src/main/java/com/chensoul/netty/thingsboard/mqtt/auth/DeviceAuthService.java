package com.chensoul.netty.thingsboard.mqtt.auth;

import java.util.Optional;
import org.thingsboard.domain.iot.device.Device;
import org.thingsboard.domain.iot.device.credential.DeviceCredentialFilter;

public interface DeviceAuthService {

	DeviceAuthResult process(DeviceCredentialFilter credentials);

	Optional<Device> findDeviceById(String deviceId);

}
