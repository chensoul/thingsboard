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
package com.chensoul.system.domain.audit.mybatis;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.domain.audit.domain.ActionStatus;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.AuditLog;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "audit_log", autoResultMap = true)
public class AuditLogEntity extends LongBaseEntity<AuditLog> {
    private String serviceId;

    private String tenantId;

    private Long merchantId;

    private Long userId;

    private String userName;

    private String traceId;

    private String requestUri;

    private String userAgent;

    private String userIp;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private JsonNode extra;

    private String entityId;

    private String entityName;

    private EntityType entityType;

    private ActionType actionType;

    private ActionStatus actionStatus;

    private String failureDetail;

    private Long costTime;

    @Override
    public AuditLog toData() {
        return JacksonUtils.convertValue(this, AuditLog.class);
    }
}
