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
package com.chensoul.system.domain.audit.domain;

import com.chensoul.data.model.BaseDataWithExtra;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuditLog extends BaseDataWithExtra<Long> {
    private String serviceId;
    private String clientId;
    private String tenantId;
    private Long merchantId;
    private Long userId;
    private String userName;

    private String traceId;
    private String requestUri;
    private String userAgent;
    private String userIp;

    private String entityId;
    private String entityName;
    private EntityType entityType;
    private ActionType actionType;
    private ActionStatus actionStatus;
    private String failureDetail;
    private Long costTime;
}
