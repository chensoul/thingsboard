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
@Builder
@Data
public class ValidateDeviceCredentialsResponseMsg {
	private TransportDeviceInfo deviceInfo;
	private final DeviceProfile deviceProfile;
	private String credential;

	public boolean hasDeviceInfo() {
		return deviceInfo != null;
	}

	public boolean hasDeviceProfile() {
		return deviceProfile != null;
	}
}
