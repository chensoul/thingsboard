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
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class NotificationTemplateConfig {

	@Valid
	@NotEmpty
	private Map<NotificationDeliveryType, NotificationDeliveryTemplate> deliveryTemplates;

	public NotificationTemplateConfig copy() {
		Map<NotificationDeliveryType, NotificationDeliveryTemplate> templates = new HashMap<>(deliveryTemplates);
		templates.replaceAll((deliveryMethod, template) -> template.copy());
		NotificationTemplateConfig copy = new NotificationTemplateConfig();
		copy.setDeliveryTemplates(templates);
		return copy;
	}

}
