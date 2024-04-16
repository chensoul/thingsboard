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

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EmailNotificationDeliveryTemplate extends NotificationDeliveryTemplate implements HasSubject {

	@NoXss(fieldName = "email subject")
	@Length(max = 250, message = "cannot be longer than 250 chars")
	@NotEmpty
	private String subject;

	private final List<TemplateValue> templateValues = List.of(
		TemplateValue.of(this::getBody, this::setBody),
		TemplateValue.of(this::getSubject, this::setSubject)
	);

	public EmailNotificationDeliveryTemplate(EmailNotificationDeliveryTemplate other) {
		super(other);
		this.subject = other.subject;
	}

	@Override
	public NotificationDeliveryMethod getDeliveryMethod() {
		return NotificationDeliveryMethod.EMAIL;
	}

	@Override
	public EmailNotificationDeliveryTemplate copy() {
		return new EmailNotificationDeliveryTemplate(this);
	}

}
