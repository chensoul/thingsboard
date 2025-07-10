/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.iot.alarm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.HasMerchantId;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

/**
 * Created by ashvayka on 11.05.17.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alarm extends BaseData<Long> implements HasName, HasTenantId, HasMerchantId {

	private String tenantId;

	private Long merchantId;

	@NoXss
	@Length
	private String name;

	@NoXss
	@Length
	private String type;
	private String originator;
	private AlarmSeverity severity;
	private boolean acknowledged;
	private boolean cleared;
	private Long assigneeId;
	private long startTs;
	private long endTs;
	private long ackTs;
	private long clearTs;
	private long assignTs;
	private transient JsonNode details;
	private boolean propagate;
	private boolean propagateToOwner;
	private boolean propagateToTenant;
	private List<String> propagateRelationTypes;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public AlarmStatus getStatus() {
		return toStatus(cleared, acknowledged);
	}

	public static AlarmStatus toStatus(boolean cleared, boolean acknowledged) {
		if (cleared) {
			return acknowledged ? AlarmStatus.CLEARED_ACK : AlarmStatus.CLEARED_UNACK;
		} else {
			return acknowledged ? AlarmStatus.ACTIVE_ACK : AlarmStatus.ACTIVE_UNACK;
		}
	}
}
