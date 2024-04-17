package org.thingsboard.domain.iot.deviceprofile;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.dao.mybatis.LongBaseEntity;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceProfileEntity extends LongBaseEntity<DeviceProfile> {
	private String tenantId;

	private String name;

	private DeviceProfileType type;

	private String image;

	private DeviceTransportType transportType;

	private DeviceProfileProvisionType provisionType;

	private String description;

	private boolean isDefault;

	private Long defaultRuleChainId;

	private String defaultQueueName;

	private JsonNode extra;

	private String provisionDeviceKey;

	private Long firmwareId;

	private Long softwareId;

	@Override
	public DeviceProfile toData() {
		return null;
	}
}
