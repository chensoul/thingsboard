/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.notification.info;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.domain.iot.alarm.AlarmSeverity;
import org.thingsboard.domain.iot.alarm.AlarmStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmNotificationInfo implements RuleOriginatedNotificationInfo {

	private String alarmType;
	private String action;
	private Long alarmId;
	private Serializable alarmOriginator;
	private String alarmOriginatorName;
	private AlarmSeverity alarmSeverity;
	private AlarmStatus alarmStatus;
	private boolean acknowledged;
	private boolean cleared;
	private Long alarmCustomerId;

	@Override
	public Map<String, String> getTemplateData() {
		return Map.of(
			"alarmType", alarmType,
			"action", action,
			"alarmId", alarmId.toString(),
			"alarmSeverity", alarmSeverity.name().toLowerCase(),
			"alarmStatus", alarmStatus.toString(),
			"alarmOriginatorId", alarmOriginator.toString()
		);
	}

	@Override
	public Long getAffectedCustomerId() {
		return alarmCustomerId;
	}

	@Override
	public Serializable getStateEntityId() {
		return alarmOriginator;
	}

}
