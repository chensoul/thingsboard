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


import com.chensoul.system.domain.notification.domain.NotificationContext;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryTemplate;
import com.chensoul.system.user.model.NotificationRecipient;

public interface NotificationChannel<R extends NotificationRecipient, T extends NotificationDeliveryTemplate> {

    void sendNotification(R recipient, T processedTemplate, NotificationContext ctx) throws Exception;

    void check(String tenantId) throws Exception;

    NotificationDeliveryMethod getDeliveryMethod();

}
