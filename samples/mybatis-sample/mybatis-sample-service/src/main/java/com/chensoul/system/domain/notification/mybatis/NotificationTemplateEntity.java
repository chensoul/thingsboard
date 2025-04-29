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

import com.baomidou.mybatisplus.annotation.TableName;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.domain.notification.domain.template.NotificationTemplate;
import com.chensoul.system.domain.notification.domain.template.NotificationTemplateConfig;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification_template")
public class NotificationTemplateEntity extends LongBaseEntity<NotificationTemplate> {

    private String tenantId;

    private String name;

    private String description;

    private NotificationType type;

    private JsonNode config;

    @Override
    public NotificationTemplate toData() {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId(id);
        notificationTemplate.setCreateTime(createTime);
        notificationTemplate.setTenantId(tenantId);
        notificationTemplate.setName(name);
        notificationTemplate.setType(type);
        notificationTemplate.setConfig(JacksonUtils.convertValue(config, NotificationTemplateConfig.class));
        return notificationTemplate;
    }

}
