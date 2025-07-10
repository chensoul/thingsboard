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
package org.thingsboard.domain.merchant.internal;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.data.service.DataValidator;
import org.thingsboard.common.validation.Validator;
import org.thingsboard.domain.merchant.Merchant;
import org.thingsboard.domain.merchant.MerchantService;
import org.thingsboard.domain.tenant.TenantService;
import org.thingsboard.domain.merchant.internal.persistence.MerchantDao;

@Component
public class MerchantValidator extends DataValidator<Merchant> {

	@Autowired
	private MerchantDao merchantDao;

	@Autowired
	private TenantService tenantService;

	@Override
	protected void validateCreate(Merchant merchant) {
		validateEntityCountPerTenant(merchant.getTenantId(), EntityType.MERCHANT);

		merchantDao.findMerchantByTenantIdAndName(merchant.getTenantId(), merchant.getName()).ifPresent(
			c -> {
				throw new DataValidationException("Customer with such title already exists!");
			}
		);
	}

	@Override
	protected Merchant validateUpdate(Merchant merchant) {
		Optional<Merchant> customerOpt = merchantDao.findMerchantByTenantIdAndName(merchant.getTenantId(), merchant.getName());
		customerOpt.ifPresent(
			c -> {
				if (!c.getId().equals(merchant.getId())) {
					throw new DataValidationException("Customer with such title already exists!");
				}
			}
		);
		return customerOpt.orElse(null);
	}

	@Override
	protected void validateDataImpl(Merchant merchant) {
		Validator.validateString(merchant.getName(), "Name can not be blank");
		if (merchant.getName().equals(MerchantService.PUBLIC_CUSTOMER_TITLE)) {
			throw new DataValidationException("'Public' title for customer is system reserved!");
		}
		if (merchant.getTenantId() == null) {
			throw new DataValidationException("Customer should be assigned to tenant!");
		} else {
			if (!tenantService.tenantExists(merchant.getTenantId())) {
				throw new DataValidationException("Customer is referencing to non-existent tenant!");
			}
		}
	}
}
