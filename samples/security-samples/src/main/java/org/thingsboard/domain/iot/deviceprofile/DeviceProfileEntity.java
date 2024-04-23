package org.thingsboard.domain.iot.deviceprofile;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.data.dao.jpa.JsonConverter;
import org.thingsboard.data.dao.jpa.LongBaseEntity;
import org.thingsboard.domain.iot.device.model.DeviceTransportType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
@Entity
@Table(name = "device_profile")
@EqualsAndHashCode(callSuper = true)
public class DeviceProfileEntity extends LongBaseEntity<DeviceProfile> {
	private String tenantId;

	private String name;

	@Enumerated(EnumType.STRING)
	private DeviceProfileType type;

	private String image;

	@Enumerated(EnumType.STRING)
	private DeviceTransportType transportType;

	@Enumerated(EnumType.STRING)
	private DeviceProfileProvisionType provisionType;

	private String description;

	private boolean defaulted;

	private Long defaultRuleChainId;

	private String defaultQueueName;

	@Convert(converter = JsonConverter.class)
	private JsonNode extra;

	private String provisionDeviceKey;

	private Long firmwareId;

	private Long softwareId;

	@Override
	public DeviceProfile toData() {
		return null;
	}
}
