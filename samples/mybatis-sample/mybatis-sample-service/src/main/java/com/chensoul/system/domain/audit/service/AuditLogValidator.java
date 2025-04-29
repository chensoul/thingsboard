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
package com.chensoul.system.domain.audit.service;

import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.audit.domain.AuditLog;
import com.chensoul.validation.DataValidator;
import org.springframework.stereotype.Component;

@Component
public class AuditLogValidator extends DataValidator<AuditLog> {

    @Override
    protected void validateDataImpl(AuditLog auditLog) {
        if (auditLog.getTenantId() == null) {
            throw new BusinessException("Tenant Id should be specified!");
        }
        if (auditLog.getUserId() == null) {
            throw new BusinessException("User Id should be specified!");
        }
    }
}
