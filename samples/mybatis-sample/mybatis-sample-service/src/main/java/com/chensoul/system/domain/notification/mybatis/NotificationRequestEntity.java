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
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.notification.domain.NotificationInfo;
import com.chensoul.system.domain.notification.domain.NotificationRequest;
import com.chensoul.system.domain.notification.domain.NotificationRequestConfig;
import com.chensoul.system.domain.notification.domain.NotificationRequestStats;
import com.chensoul.system.domain.notification.domain.NotificationRequestStatus;
import com.chensoul.system.domain.notification.domain.template.NotificationTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification_request")
public class NotificationRequestEntity extends LongBaseEntity<NotificationRequest> {

    private String tenantId;

    private String targets;

    private Long templateId;

    private JsonNode template;

    private JsonNode info;

    private JsonNode config;

    private String entityId;

    private EntityType entityType;

    private Long ruleId;

    private NotificationRequestStatus status;

    private JsonNode stats;

    @Override
    public NotificationRequest toData() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setId(id);
        notificationRequest.setCreateTime(createTime);
        notificationRequest.setTenantId(tenantId);
        notificationRequest.setTargets(listFromString(targets, Long::valueOf));
        notificationRequest.setTemplateId(templateId);
        notificationRequest.setTemplate(JacksonUtils.convertValue(template, NotificationTemplate.class));
        notificationRequest.setInfo(JacksonUtils.convertValue(info, NotificationInfo.class));
        notificationRequest.setConfig(JacksonUtils.convertValue(config, NotificationRequestConfig.class));
        if (entityId != null) {
            notificationRequest.setEntityId(entityId);
        }
        notificationRequest.setRuleId(ruleId);
        notificationRequest.setStatus(status);
        notificationRequest.setStats(JacksonUtils.convertValue(stats, NotificationRequestStats.class));
        return notificationRequest;
    }
}
