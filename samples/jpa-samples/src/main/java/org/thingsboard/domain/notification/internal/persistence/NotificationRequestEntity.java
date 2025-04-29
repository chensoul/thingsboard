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
package org.thingsboard.domain.notification.internal.persistence;

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
import org.thingsboard.domain.notification.NotificationRequest;
import org.thingsboard.domain.notification.NotificationRequestConfig;
import org.thingsboard.domain.notification.NotificationRequestStats;
import org.thingsboard.domain.notification.NotificationRequestStatus;
import org.thingsboard.domain.notification.NotificationInfo;
import org.thingsboard.domain.notification.internal.template.NotificationTemplate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notification_request")
public class NotificationRequestEntity extends LongBaseEntity<NotificationRequest> {

	private String tenantId;

	private String targets;

	@Column(nullable = false)
	private Long templateId;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "jsonb")
	private JsonNode template;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "jsonb")
	private JsonNode info;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "jsonb")
	private JsonNode config;

	private String entityId;

	@Enumerated(EnumType.STRING)
	private EntityType entityType;

	private Long ruleId;

	@Column(nullable = false)
	private NotificationRequestStatus status;

	@Convert(converter = JsonConverter.class)
	private JsonNode stats;

	@Override
	public NotificationRequest toData() {
		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setId(id);
		notificationRequest.setCreatedTime(createdTime);
		notificationRequest.setTenantId(tenantId);
		notificationRequest.setTargets(listFromString(targets, Long::valueOf));
		notificationRequest.setTemplateId(templateId);
		notificationRequest.setTemplate(JacksonUtil.convertValue(template, NotificationTemplate.class));
		notificationRequest.setInfo(JacksonUtil.convertValue(info, NotificationInfo.class));
		notificationRequest.setConfig(JacksonUtil.convertValue(config, NotificationRequestConfig.class));
		if (entityId != null) {
			notificationRequest.setEntityId(entityId);
		}
		notificationRequest.setRuleId(ruleId);
		notificationRequest.setStatus(status);
		notificationRequest.setStats(JacksonUtil.convertValue(stats, NotificationRequestStats.class));
		return notificationRequest;
	}
}
