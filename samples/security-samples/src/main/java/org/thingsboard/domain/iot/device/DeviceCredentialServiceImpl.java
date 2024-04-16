package org.thingsboard.domain.iot.device;

import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
public class DeviceCredentialServiceImpl implements DeviceCredentialService {
	@Override
	public DeviceCredential findDeviceCredentialsByCredentialsId(String sha3Hash) {
		return null;
	}

	@Override
	public void createDeviceCredential(String tenantId, DeviceCredential deviceCredentials) {

	}
}
