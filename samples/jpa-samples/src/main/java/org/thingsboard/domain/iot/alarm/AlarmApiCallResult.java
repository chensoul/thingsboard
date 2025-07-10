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

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;


@Data
public class AlarmApiCallResult implements Serializable {

	private final boolean successful;
	private final boolean created;
	private final boolean modified;
	private final boolean cleared;
	private final boolean deleted;
	private final AlarmInfo alarm;
	private final Alarm old;

	@Builder
	private AlarmApiCallResult(boolean successful, boolean created, boolean modified, boolean cleared, boolean deleted, AlarmInfo alarm, Alarm old) {
		this.successful = successful;
		this.created = created;
		this.modified = modified;
		this.cleared = cleared;
		this.deleted = deleted;
		this.alarm = alarm;
		this.old = old;
	}

	public AlarmApiCallResult(AlarmApiCallResult other) {
		this.successful = other.successful;
		this.created = other.created;
		this.modified = other.modified;
		this.cleared = other.cleared;
		this.deleted = other.deleted;
		this.alarm = other.alarm;
		this.old = other.old;
	}

	public boolean isSeverityChanged() {
		if (alarm == null || old == null) {
			return false;
		} else {
			return !alarm.getSeverity().equals(old.getSeverity());
		}
	}

	public boolean isAcknowledged() {
		if (alarm == null || old == null) {
			return false;
		} else {
			return alarm.isAcknowledged() != old.isAcknowledged();
		}
	}

	public AlarmSeverity getOldSeverity() {
		return isSeverityChanged() ? old.getSeverity() : null;
	}

	public boolean isPropagationChanged() {
		if (created) {
			return true;
		}
		if (alarm == null || old == null) {
			return false;
		}
		return (alarm.isPropagate() != old.isPropagate()) ||
			   (alarm.isPropagateToOwner() != old.isPropagateToOwner()) ||
			   (alarm.isPropagateToTenant() != old.isPropagateToTenant()) ||
			   (!alarm.getPropagateRelationTypes().equals(old.getPropagateRelationTypes()));
	}

}
