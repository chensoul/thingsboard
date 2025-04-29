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
package com.chensoul.system.domain.merchant;

import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import static com.chensoul.system.ControllerConstants.MERCHANT_ID;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.infrastructure.common.BaseController;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getTenantId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MerchantController extends BaseController {
    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'MERCHANT_USER')")
    @GetMapping(value = "/merchant/{merchantId}")
    public Merchant getMerchantById(@PathVariable(MERCHANT_ID) Long merchantId) {
        return checkMerchantId(merchantId);
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @PostMapping(value = "/merchant")
    public Merchant saveMerchant(@RequestBody Merchant merchant) {
        merchant.setTenantId(getTenantId());
        Merchant old = checkMerchantId(merchant.getId());
        return (Merchant) auditLogService.doAndLog(merchant, old, EntityType.MERCHANT, ActionType.ADD, (t) -> merchantService.saveMerchant(merchant));
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @DeleteMapping(value = "/merchant/{merchantId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteMerchant(@PathVariable(MERCHANT_ID) Long merchantId) {
        Merchant old = checkMerchantId(merchantId);
        auditLogService.doAndLog(old, EntityType.MERCHANT, ActionType.DELETE, (t) -> merchantService.deleteMerchant(old));
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/merchants")
    public PageData<Merchant> getTenants(PageLink pageLink) {
        return merchantService.findTenant(getTenantId(), pageLink);
    }
}
