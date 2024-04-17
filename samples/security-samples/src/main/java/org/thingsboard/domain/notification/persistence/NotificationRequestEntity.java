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
package org.thingsboard.domain.notification.persistence;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.dao.mybatis.LongBaseEntity;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.notification.NotificationRequest;
import org.thingsboard.domain.notification.NotificationRequestConfig;
import org.thingsboard.domain.notification.NotificationRequestStats;
import org.thingsboard.domain.notification.NotificationRequestStatus;
import org.thingsboard.domain.notification.info.NotificationInfo;
import org.thingsboard.domain.notification.template.NotificationTemplate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification_request", autoResultMap = true)
public class NotificationRequestEntity extends LongBaseEntity<NotificationRequest> {

	private String tenantId;

	private String targets;

	private Long templateId;

	@TableField(typeHandler = JacksonTypeHandler.class)
	private JsonNode template;

	@TableField(typeHandler = JacksonTypeHandler.class)
	private JsonNode info;

	@TableField(typeHandler = JacksonTypeHandler.class)
	private JsonNode config;

	private String entityId;

	private EntityType entityType;

	private Long ruleId;

	private NotificationRequestStatus status;

	@TableField(typeHandler = JacksonTypeHandler.class)
	private JsonNode stats;

	@Override
	public NotificationRequest toData() {
		NotificationRequest notificationRequest = new NotificationRequest();
		notificationRequest.setId(id);
		notificationRequest.setCreatedTime(createdTime);
		notificationRequest.setTenantId(tenantId);
		notificationRequest.setTargets(listFromString(targets, Long::valueOf));
		notificationRequest.setTemplateId(templateId);
		notificationRequest.setTemplate(fromJson(template, NotificationTemplate.class));
		notificationRequest.setInfo(fromJson(info, NotificationInfo.class));
		notificationRequest.setConfig(fromJson(config, NotificationRequestConfig.class));
		if (entityId != null) {
			notificationRequest.setEntityId(entityId);
		}
		notificationRequest.setRuleId(ruleId);
		notificationRequest.setStatus(status);
		notificationRequest.setStats(fromJson(stats, NotificationRequestStats.class));
		return notificationRequest;
	}
}
