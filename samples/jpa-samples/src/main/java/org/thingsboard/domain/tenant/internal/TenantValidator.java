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
package org.thingsboard.domain.tenant.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.data.service.DataValidator;
import org.thingsboard.domain.tenant.Tenant;
import org.thingsboard.domain.tenant.internal.persistence.TenantDao;

@Component
public class TenantValidator extends DataValidator<Tenant> {

	@Autowired
	private TenantDao tenantDao;

	@Override
	protected void validateDataImpl(Tenant tenant) {
	}

	@Override
	protected Tenant validateUpdate(Tenant tenant) {
		Tenant old = tenantDao.findById(tenant.getId());
		if (old == null) {
			throw new DataValidationException("Can't update non existing tenant!");
		}
		return old;
	}
}
