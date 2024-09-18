package org.thingsboard.domain.iot.device;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface DeviceCredentialService {
	DeviceCredential findDeviceCredentialsByCredentialsId(String sha3Hash);

  void createDeviceCredential(String tenantId, DeviceCredential deviceCredentials);
}
