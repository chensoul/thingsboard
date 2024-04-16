package org.thingsboard.domain.message;

import lombok.Data;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
public class TransportApiRequestMsg {
	private ValidateDeviceTokenRequestMsg validateTokenRequestMsg;
	private ValidateBasicMqttCredRequestMsg basicMqttCredRequestMsg;
}
