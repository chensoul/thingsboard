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
package org.thingsboard.data.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.validation.ConstraintValidator;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.domain.usage.ApiLimitService;
import org.thingsboard.domain.usage.limit.EntitiesLimitException;
import org.thingsboard.server.security.permission.AccessControlService;

@Slf4j
public abstract class DataValidator<D extends BaseData<?>> {
	@Autowired
	@Lazy
	protected ApiLimitService apiLimitService;

	@Autowired
	protected AccessControlService accessControlService;

	public D validate(D data) {
		try {
			if (data == null) {
				throw new DataValidationException("Data object can't be null!");
			}
			ConstraintValidator.validateFields(data);

			validateDataImpl(data);

			D old = null;
			if (data.getId() == null) {
				validateCreate(data);
			} else {
				old = validateUpdate(data);
			}

			return old;
		} catch (DataValidationException e) {
			log.error("{} object is invalid: [{}]", data == null ? "Data" : data.getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	protected void validateDataImpl(D data) {
	}

	protected void validateCreate(D data) {
	}

	protected D validateUpdate(D data) {
		return data;
	}

	public void validateDelete(D data) {
		if (data == null) {
			throw new DataValidationException("Data object can't be null!");
		}
		if (data.getId() == null) {
			throw new DataValidationException("Can't delete object without id!");
		}
	}

	protected boolean isSameData(D existentData, D actualData) {
		return actualData.getId() != null && existentData.getId().equals(actualData.getId());
	}

	protected void validateEntityCountPerTenant(String tenantId,
												EntityType entityType) {
		if (!apiLimitService.checkEntitiesLimit(tenantId, entityType)) {
			throw new EntitiesLimitException(tenantId, entityType);
		}
	}
}
