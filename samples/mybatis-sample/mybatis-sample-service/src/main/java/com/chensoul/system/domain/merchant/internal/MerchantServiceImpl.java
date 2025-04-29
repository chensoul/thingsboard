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

import com.chensoul.data.event.DeleteEntityEvent;
import com.chensoul.data.event.SaveEntityEvent;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.system.domain.merchant.Merchant;
import com.chensoul.system.domain.merchant.MerchantService;
import com.chensoul.system.domain.merchant.internal.persistence.MerchantDao;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class MerchantServiceImpl implements MerchantService {
    private final ApplicationEventPublisher eventPublisher;
    private final MerchantDao merchantDao;
    private final MerchantValidator merchantValidator;

    @Override
    public Merchant findMerchantById(Long merchantId) {
        return merchantDao.findById(merchantId);
    }

    @Override
    public Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String customerName) {
        return merchantDao.findMerchantByTenantIdAndName(tenantId, customerName);
    }

    @Override
    public Merchant saveMerchant(Merchant merchant) {
        merchantValidator.validate(merchant);
        Merchant save = merchantDao.save(merchant);

        eventPublisher.publishEvent(SaveEntityEvent.builder()
            .entityId(save.getId()).entity(save).created(true).build());
        return save;
    }

    @Override
    public void deleteMerchant(Merchant merchant) {
        if (merchant != null) {
            merchantDao.removeById(merchant.getId());
            eventPublisher.publishEvent(DeleteEntityEvent.builder().tenantId(merchant.getTenantId())
                .entity(merchant).entityId(merchant.getId()).build());
        }
    }

    @Override
    public void deleteMerchantByTenantId(String tenantId) {
        merchantDao.removeByTenantId(tenantId);
    }

    @Override
    public PageData<Merchant> findTenant(String tenantId, PageLink pageLink) {
        return merchantDao.findTenants(tenantId, pageLink);
    }
}
