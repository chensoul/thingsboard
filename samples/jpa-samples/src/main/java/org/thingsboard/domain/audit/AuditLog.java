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
package org.thingsboard.domain.audit;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.EntityType;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuditLog extends BaseData<Long> {
	private String serviceId;
	private String serviceName;
	private String tenantId;
	private Long merchantId;
	private Long userId;
	private String userName;
	private String entityId;
	private EntityType entityType;
	private ActionType actionType;
	private JsonNode actionData;
	private ActionStatus actionStatus;
	private String failureDetail;
}
