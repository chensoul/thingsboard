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
package org.thingsboard.domain.audit.internal;

import org.springframework.stereotype.Component;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.data.service.DataValidator;
import org.thingsboard.domain.audit.AuditLog;

@Component
public class AuditLogValidator extends DataValidator<AuditLog> {

	@Override
	protected void validateDataImpl( AuditLog auditLog) {
		if (auditLog.getTenantId() == null) {
			throw new DataValidationException("Tenant Id should be specified!");
		}
		if (auditLog.getUserId() == null) {
			throw new DataValidationException("User Id should be specified!");
		}
	}
}
