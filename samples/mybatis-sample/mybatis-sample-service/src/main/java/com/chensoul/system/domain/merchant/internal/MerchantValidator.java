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
package com.chensoul.system.domain.merchant.internal;

import com.chensoul.data.validation.Validators;
import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.merchant.Merchant;
import com.chensoul.system.domain.merchant.MerchantService;
import com.chensoul.system.domain.merchant.internal.persistence.MerchantDao;
import com.chensoul.system.domain.tenant.service.TenantService;
import com.chensoul.validation.DataValidator;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MerchantValidator extends DataValidator<Merchant> {

    @Autowired
    private MerchantDao merchantDao;

    @Autowired
    private TenantService tenantService;

    @Override
    protected void validateCreate(Merchant merchant) {
//        validateEntityCountPerTenant(merchant.getTenantId(), EntityType.MERCHANT);

        merchantDao.findMerchantByTenantIdAndName(merchant.getTenantId(), merchant.getName()).ifPresent(
            c -> {
                throw new BusinessException("Customer with such title already exists!");
            }
        );
    }

    @Override
    protected Merchant validateUpdate(Merchant merchant) {
        Optional<Merchant> customerOpt = merchantDao.findMerchantByTenantIdAndName(merchant.getTenantId(), merchant.getName());
        customerOpt.ifPresent(
            c -> {
                if (!c.getId().equals(merchant.getId())) {
                    throw new BusinessException("Customer with such title already exists!");
                }
            }
        );
        return customerOpt.orElse(null);
    }

    @Override
    protected void validateDataImpl(Merchant merchant) {
        Validators.validateString(merchant.getName(), "Name can not be blank");
        if (merchant.getName().equals(MerchantService.PUBLIC_CUSTOMER_TITLE)) {
            throw new BusinessException("'Public' title for customer is system reserved!");
        }
        if (merchant.getTenantId() == null) {
            throw new BusinessException("Customer should be assigned to tenant!");
        } else {
            if (!tenantService.tenantExists(merchant.getTenantId())) {
                throw new BusinessException("Customer is referencing to non-existent tenant!");
            }
        }
    }
}
