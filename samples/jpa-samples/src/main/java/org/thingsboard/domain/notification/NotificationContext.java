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
package org.thingsboard.domain.notification;

import com.google.common.base.Strings;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.common.util.TemplateUtils;
import org.thingsboard.domain.setting.NotificationDeliveryMethodConfig;
import org.thingsboard.domain.setting.NotificationSetting;
import org.thingsboard.domain.notification.internal.template.NotificationDeliveryTemplate;
import org.thingsboard.domain.notification.internal.template.NotificationDeliveryMethod;
import org.thingsboard.domain.notification.internal.template.NotificationTemplate;
import org.thingsboard.domain.notification.internal.template.NotificationType;

@SuppressWarnings("unchecked")
public class NotificationContext {

	@Getter
	private final String tenantId;
	private final NotificationSetting settings;
	private final NotificationSetting systemSettings;
	@Getter
	private final NotificationRequest request;
	@Getter
	private final Set<NotificationDeliveryMethod> deliveryTypes;
	@Getter
	private final NotificationTemplate notificationTemplate;
	@Getter
	private final NotificationType notificationType;

	private final Map<NotificationDeliveryMethod, NotificationDeliveryTemplate> deliveryTemplates;
	@Getter
	private final NotificationRequestStats stats;

	@Builder
	public NotificationContext(String tenantId, NotificationRequest request, Set<NotificationDeliveryMethod> deliveryTypes,
                             NotificationTemplate template, NotificationSetting settings, NotificationSetting systemSettings) {
		this.tenantId = tenantId;
		this.request = request;
		this.deliveryTypes = deliveryTypes;
		this.settings = settings;
		this.systemSettings = systemSettings;
		this.notificationTemplate = template;
		this.notificationType = template.getType();
		this.deliveryTemplates = new EnumMap<>(NotificationDeliveryMethod.class);
		this.stats = new NotificationRequestStats();
		init();
	}

	private void init() {
		notificationTemplate.getConfig().getDeliveryTemplates().forEach((type, template) -> {
			if (template.isEnabled()) {
				template = processTemplate(template, null); // processing template with immutable params
				deliveryTemplates.put(template.getDeliveryMethod(), template);
			}
		});
	}

	public <C extends NotificationDeliveryMethodConfig> C getDeliveryMethodConfig(NotificationDeliveryMethod deliveryMethod) {
		NotificationSetting settings;
		if (deliveryMethod == NotificationDeliveryMethod.MOBILE_APP) {
			settings = this.systemSettings;
		} else {
			settings = this.settings;
		}
		return (C) settings.getDeliveryMethodsConfigs().get(deliveryMethod);
	}

	public <T extends NotificationDeliveryTemplate> T getProcessedTemplate(NotificationDeliveryMethod deliveryMethod, NotificationRecipient recipient) {
		T template = (T) deliveryTemplates.get(deliveryMethod);
		if (recipient != null) {
			Map<String, String> additionalTemplateContext = createTemplateContextForRecipient(recipient);
			if (template.getTemplateValues().stream().anyMatch(value -> value.containsParams(additionalTemplateContext.keySet()))) {
				template = processTemplate(template, additionalTemplateContext);
			}
		}
		return template;
	}

	private <T extends NotificationDeliveryTemplate> T processTemplate(T template, Map<String, String> additionalTemplateContext) {
		Map<String, String> templateContext = new HashMap<>();
		if (request.getInfo() != null) {
			templateContext.putAll(request.getInfo().getTemplateData());
		}
		if (additionalTemplateContext != null) {
			templateContext.putAll(additionalTemplateContext);
		}
		if (templateContext.isEmpty()) return template;

		template = (T) template.copy();
		template.getTemplateValues().forEach(templateValue -> {
			String value = templateValue.get();
			if (StringUtils.isNotEmpty(value)) {
				value = TemplateUtils.processTemplate(value, templateContext);
				templateValue.set(value);
			}
		});
		return template;
	}

	private Map<String, String> createTemplateContextForRecipient(NotificationRecipient recipient) {
		return Map.of(
			"recipientName", recipient.getName(),
			"recipientEmail", Strings.nullToEmpty(recipient.getEmail())
		);
	}

}
