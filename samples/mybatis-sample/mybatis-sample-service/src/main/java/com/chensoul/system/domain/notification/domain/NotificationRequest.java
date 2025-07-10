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
package com.chensoul.system.domain.notification.domain;

import com.chensoul.data.model.BaseData;
import com.chensoul.data.model.HasName;
import com.chensoul.data.model.HasTenantId;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.notification.domain.template.NotificationTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest extends BaseData<Long> implements HasTenantId, HasName {

    List<Long> targets;
    private String tenantId;
    private Long templateId;

    @Valid
    private NotificationTemplate template;
    @Valid
    private NotificationInfo info;

    @Valid
    private NotificationRequestConfig config;

    private EntityType entityType;
    private Serializable entityId;

    private Long ruleId;

    private NotificationRequestStatus status;

    private NotificationRequestStats stats;

    @JsonIgnore
    @Override
    public String getName() {
        return "To targets " + targets;
    }

    @JsonIgnore
    public Long getSenderId() {
        return entityType.equals(EntityType.USER) ? (Long) entityId : null;
    }

    @JsonIgnore
    public boolean isSent() {
        return status == NotificationRequestStatus.SENT;
    }

    @JsonIgnore
    public boolean isScheduled() {
        return status == NotificationRequestStatus.SCHEDULED;
    }

}
