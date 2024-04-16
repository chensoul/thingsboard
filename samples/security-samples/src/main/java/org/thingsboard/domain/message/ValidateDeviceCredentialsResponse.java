package org.thingsboard.domain.message;

import lombok.Builder;
import lombok.Data;
import org.thingsboard.domain.iot.deviceprofile.DeviceProfile;
import org.thingsboard.transport.auth.TransportDeviceInfo;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
@Builder
public class ValidateDeviceCredentialsResponse {
	private final TransportDeviceInfo deviceInfo;
	private final DeviceProfile deviceProfile;
	private final String credential;

	public boolean hasDeviceInfo() {
		return deviceInfo != null;
	}
}
