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
package org.thingsboard.domain.audit.internal.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.data.dao.jpa.JsonConverter;
import org.thingsboard.data.dao.jpa.LongBaseEntity;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.audit.ActionStatus;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.audit.AuditLog;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "audit_log")
public class AuditLogEntity extends LongBaseEntity<AuditLog> {
	private String serviceId;

	private String serviceName;

	private String tenantId;

	private Long merchantId;

	private Long userId;

	private String userName;

	@Enumerated(EnumType.STRING)
	private EntityType entityType;

	private String entityId;

	@Enumerated(EnumType.STRING)
	private ActionType actionType;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "jsonb")
	private JsonNode actionData;

	@Enumerated(EnumType.STRING)
	private ActionStatus actionStatus;

	private String failureDetail;

	@Override
	public AuditLog toData() {
		return JacksonUtil.convertValue(this, AuditLog.class);
	}
}
