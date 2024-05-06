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
package org.thingsboard.domain.notification.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.thingsboard.domain.notification.rule.trigger.config.NotificationRuleTriggerType;

@JsonIgnoreProperties
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "triggerType", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY, defaultImpl = DefaultNotificationRuleRecipientsConfig.class)
@JsonSubTypes({
	@Type(name = "ALARM", value = EscalatedNotificationRuleRecipientsConfig.class),
})
@Data
public abstract class NotificationRuleRecipientsConfig implements Serializable {

	@NotNull
	private NotificationRuleTriggerType triggerType;

	@JsonIgnore
	public abstract Map<Integer, List<Long>> getTargetsTable();

}
