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

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.BaseData;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Event extends BaseData<Long> {
	protected final String tenantId;
	protected final String entityId;
	protected final String serviceId;

	public Event(String tenantId, String entityId, String serviceId, Long id, long ts) {
		super();
		if (id != null) {
			this.id = id;
		}
		this.tenantId = tenantId != null ? tenantId : SYS_TENANT_ID;
		this.entityId = entityId;
		this.serviceId = serviceId;
		this.createdTime = ts;
	}

	public abstract EventType getType();

}
