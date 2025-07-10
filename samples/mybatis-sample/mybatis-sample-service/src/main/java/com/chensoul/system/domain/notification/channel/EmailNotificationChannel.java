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
package com.chensoul.system.domain.notification.channel;

import com.chensoul.system.domain.notification.channel.mail.Email;
import com.chensoul.system.domain.notification.channel.mail.MailService;
import com.chensoul.system.domain.notification.domain.NotificationContext;
import com.chensoul.system.domain.notification.domain.template.EmailNotificationDeliveryTemplate;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import com.chensoul.system.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    public NotificationDeliveryMethod getDeliveryMethod() {
        return NotificationDeliveryMethod.EMAIL;
    }

}
