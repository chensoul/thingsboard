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
package org.thingsboard.domain.notification.channels;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.notification.NotificationContext;
import org.thingsboard.domain.notification.template.EmailNotificationDeliveryTemplate;
import org.thingsboard.domain.notification.template.NotificationDeliveryType;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.server.mail.Email;
import org.thingsboard.server.mail.MailService;

@Component
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel<User, EmailNotificationDeliveryTemplate> {

	private final MailService mailService;

	@Override
	public void sendNotification(User recipient, EmailNotificationDeliveryTemplate processedTemplate, NotificationContext ctx) throws Exception {
		mailService.send(recipient.getTenantId(), null, Email.builder()
			.to(recipient.getEmail())
			.subject(processedTemplate.getSubject())
			.body(processedTemplate.getBody())
			.html(true)
			.build());
	}

	@Override
	public void check(String tenantId) throws Exception {
		if (!mailService.isConfigured(tenantId)) {
			throw new RuntimeException("Mail server is not configured");
		}
	}

	@Override
	public NotificationDeliveryType getDeliveryMethod() {
		return NotificationDeliveryType.EMAIL;
	}

}
