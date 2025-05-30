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
package com.chensoul.system.domain.notification.mybatis;

import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryTemplate;
import java.util.Collection;
import java.util.Map;
import lombok.Data;

@Data
public class NotificationRequestPreview {

    private Map<NotificationDeliveryMethod, NotificationDeliveryTemplate> processedTemplates;
    private int totalRecipientsCount;
    private Map<String, Integer> recipientsCountByTarget;
    private Collection<String> recipientsPreview;

}
