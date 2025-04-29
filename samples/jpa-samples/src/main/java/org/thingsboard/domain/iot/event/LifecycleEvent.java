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
package org.thingsboard.domain.iot.event;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class LifecycleEvent extends Event {

	private static final long serialVersionUID = -3247420461850911549L;

	@Getter
	private final String lcEventType;

	@Getter
	private final boolean success;
	@Getter
	@Setter
	private String error;

	@Builder
	private LifecycleEvent(String tenantId, String entityId, String serviceId,
						   Long id, long ts,
						   String lcEventType, boolean success, String error) {
		super(tenantId, entityId, serviceId, id, ts);
		this.lcEventType = lcEventType;
		this.success = success;
		this.error = error;
	}

	@Override
	public EventType getType() {
		return EventType.LC_EVENT;
	}


}
