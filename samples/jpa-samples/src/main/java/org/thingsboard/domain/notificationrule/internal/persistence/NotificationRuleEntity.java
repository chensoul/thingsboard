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
package org.thingsboard.domain.notificationrule.internal.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.data.dao.jpa.JsonConverter;
import org.thingsboard.data.dao.jpa.LongBaseEntity;
import org.thingsboard.domain.notificationrule.NotificationRule;
import org.thingsboard.domain.notificationrule.NotificationRuleTriggerType;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notification_rule")
public class NotificationRuleEntity extends LongBaseEntity<NotificationRule> {

	private String tenantId;

	private String name;

	private boolean enabled;

	private Long templateId;

	@Enumerated(EnumType.STRING)
	private NotificationRuleTriggerType triggerType;

	@Convert(converter = JsonConverter.class)
	private JsonNode triggerConfig;

	@Convert(converter = JsonConverter.class)
	private JsonNode recipientsConfig;

	@Convert(converter = JsonConverter.class)
	private JsonNode additionalConfig;

	@Override
	public NotificationRule toData() {
		return JacksonUtil.convertValue(this, NotificationRule.class);
	}
}
