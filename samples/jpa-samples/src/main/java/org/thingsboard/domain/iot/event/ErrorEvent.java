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
package org.thingsboard.domain.iot.event;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class ErrorEvent extends Event {
	private static final long serialVersionUID = 960461434033192571L;

	@Getter
	@Setter
	private String method;

	@Getter
	@Setter
	private String error;

	@Builder
	private ErrorEvent(String tenantId, String entityId, String serviceId, Long id, long ts, String method, String error) {
		super(tenantId, entityId, serviceId, id, ts);
		this.method = method;
		this.error = error;
	}

	@Override
	public EventType getType() {
		return EventType.ERROR;
	}
}
