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
package org.thingsboard.domain.notification.template;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationTemplate extends BaseData<Long> implements HasTenantId, HasName {

	private String tenantId;

	@NoXss
	@NotEmpty
	@Length(max = 255, message = "cannot be longer than 255 chars")
	private String name;

	@NoXss
	@Length(max = 500, message = "cannot be longer than 500 chars")
	private String description;

	@NoXss
	@NotNull
	private NotificationType type;
	@Valid
	@NotNull
	private NotificationTemplateConfig config;
}
