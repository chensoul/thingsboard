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
package org.thingsboard.domain.notificationrule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationRule extends BaseData<Long> implements HasTenantId, HasName, Serializable {

	private String tenantId;
	@NotBlank
	@NoXss
	@Length(max = 255, message = "cannot be longer than 255 chars")
	private String name;

	private String description;

	private boolean enabled;

	@NotNull
	private Long templateId;

	@NotNull
	private NotificationRuleTriggerType triggerType;
	@NotNull
	@Valid
	private NotificationRuleTriggerConfig triggerConfig;
	@NotNull
	@Valid
	private NotificationRuleRecipientsConfig recipientsConfig;

	private JsonNode additionalConfig;

	@JsonIgnore
	@AssertTrue(message = "trigger type not matching")
	public boolean isValid() {
		return triggerType == triggerConfig.getTriggerType() &&
			   triggerType == recipientsConfig.getTriggerType();
	}

	@JsonIgnore
	public String getDeduplicationKey() {
		String targets = recipientsConfig.getTargetsTable().values().stream()
			.flatMap(List::stream).sorted().map(Object::toString)
			.collect(Collectors.joining(","));
		return String.join(":", targets, triggerConfig.getDeduplicationKey());
	}

}
