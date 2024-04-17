package org.thingsboard.transport;

import org.thingsboard.domain.iot.device.model.DeviceTransportType;
import org.thingsboard.domain.message.GetDeviceCredentialsRequestMsg;
import org.thingsboard.domain.message.GetDeviceCredentialsResponseMsg;
import org.thingsboard.domain.message.GetDeviceRequestMsg;
import org.thingsboard.domain.message.GetDeviceResponseMsg;
import org.thingsboard.domain.message.ValidateBasicMqttCredRequestMsg;
import org.thingsboard.domain.message.ValidateDeviceCredentialsResponse;
import org.thingsboard.domain.message.ValidateDeviceTokenRequestMsg;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface TransportService {
	GetDeviceResponseMsg getDevice(GetDeviceRequestMsg requestMsg);

	GetDeviceCredentialsResponseMsg getDeviceCredential(GetDeviceCredentialsRequestMsg requestMsg);

	void process(DeviceTransportType transportType, ValidateDeviceTokenRequestMsg msg,
				 TransportServiceCallback<ValidateDeviceCredentialsResponse> callback);

	void process(DeviceTransportType transportType, ValidateBasicMqttCredRequestMsg msg,
				 TransportServiceCallback<ValidateDeviceCredentialsResponse> callback);
}
