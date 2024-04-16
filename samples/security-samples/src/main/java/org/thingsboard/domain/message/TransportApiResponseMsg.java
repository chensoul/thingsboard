package org.thingsboard.domain.message;

import lombok.Builder;
import lombok.Data;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Builder
@Data
public class TransportApiResponseMsg {
	private ValidateDeviceCredentialsResponseMsg validateCredResponseMsg;
}
