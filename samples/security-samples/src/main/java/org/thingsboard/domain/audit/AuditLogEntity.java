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
package org.thingsboard.domain.audit;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.dao.jpa.JsonConverter;
import org.thingsboard.common.dao.mybatis.LongBaseEntity;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.util.JacksonUtil;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "audit_log")
public class AuditLogEntity extends LongBaseEntity<AuditLog> {
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
