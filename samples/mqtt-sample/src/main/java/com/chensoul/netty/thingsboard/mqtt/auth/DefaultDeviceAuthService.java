package com.chensoul.netty.thingsboard.mqtt.auth;

import java.util.Optional;
import jdk.jfr.Registered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thingsboard.domain.iot.device.Device;
import org.thingsboard.domain.iot.device.DeviceCredential;
import org.thingsboard.domain.iot.device.credential.DeviceCredentialFilter;

@Service
@Slf4j
@Registered
public class DefaultDeviceAuthService implements DeviceAuthService {

//    private final DeviceService deviceService;
//
//    private final DeviceCredentialService deviceCredentialsService;

	@Override
	public DeviceAuthResult process(DeviceCredentialFilter credentialsFilter) {
		log.trace("Lookup device credentials using filter {}", credentialsFilter);
		DeviceCredential credentials = new DeviceCredential();//deviceCredentialService.findDeviceCredentialsByCredentialsId(credentialsFilter.getCredentialId());
		credentials.setDeviceId("1");
		credentials.setCredentialId(credentialsFilter.getCredentialId());
		credentials.setCredentialType(credentialsFilter.getCredentialType());

		if (credentials != null) {
			log.trace("Credentials found {}", credentials);
			if (credentials.getCredentialType() == credentialsFilter.getCredentialType()) {
				switch (credentials.getCredentialType()) {
					case ACCESS_TOKEN:
						// Credentials ID matches Credentials value in this
						// primitive case;
						return DeviceAuthResult.of(credentials.getDeviceId());
					case X509_CERTIFICATE:
						return DeviceAuthResult.of(credentials.getDeviceId());
					case LWM2M_CREDENTIALS:
						return DeviceAuthResult.of(credentials.getDeviceId());
					default:
						return DeviceAuthResult.of("Credentials Type is not supported yet!");
				}
			} else {
				return DeviceAuthResult.of("Credentials Type mismatch!");
			}
		} else {
			log.trace("Credentials not found!");
			return DeviceAuthResult.of("Credentials Not Found!");
		}
	}

	@Override
	public Optional<Device> findDeviceById(String deviceId) {
		Device device = new Device();
		device.setId(deviceId);
		return Optional.of(device);
	}

}
