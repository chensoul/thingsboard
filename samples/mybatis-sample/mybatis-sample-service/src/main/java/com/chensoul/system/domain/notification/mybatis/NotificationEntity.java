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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.domain.notification.domain.Notification;
import com.chensoul.system.domain.notification.domain.NotificationInfo;
import com.chensoul.system.domain.notification.domain.NotificationStatus;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification", autoResultMap = true)
public class NotificationEntity extends LongBaseEntity<Notification> {

    private Long requestId;

    private Long recipientId;

    private NotificationType type;

    private NotificationDeliveryMethod deliveryMethod;

    private String subject;

    private String text;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private JsonNode config;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private JsonNode info;

    private NotificationStatus status;

    @Override
    public Notification toData() {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setCreateTime(createTime);
        notification.setRequestId(requestId);
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setDeliveryMethod(deliveryMethod);
        notification.setSubject(subject);
        notification.setText(text);
        notification.setConfig(config);
        notification.setInfo(JacksonUtils.convertValue(info, NotificationInfo.class));
        notification.setStatus(status);
        return notification;
    }

}
