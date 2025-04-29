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
package org.thingsboard.domain.merchant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import static org.thingsboard.common.ControllerConstants.MERCHANT_ID;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.BaseController;
import static org.thingsboard.server.security.SecurityUtils.getTenantId;
import org.thingsboard.server.security.permission.Operation;

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
	@RequestMapping(value = "/merchant/{merchantId}", method = RequestMethod.GET)
	public Merchant getMerchantById(@PathVariable(MERCHANT_ID) Long merchantId) {
		return checkMerchantId(merchantId, Operation.READ);
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/merchant", method = RequestMethod.POST)
	public Merchant saveMerchant(@RequestBody Merchant merchant) throws Exception {
		merchant.setTenantId(getTenantId());
		Merchant old = checkMerchantId(merchant.getId(), Operation.WRITE);
		return doSaveAndLog(merchant, old, EntityType.MERCHANT, (t) -> merchantService.saveMerchant(merchant));
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/merchant/{merchantId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteMerchant(@PathVariable(MERCHANT_ID) Long merchantId) throws Exception {
		Merchant old = checkMerchantId(merchantId, Operation.DELETE);
		doDeleteAndLog(old, EntityType.USER, (t) -> merchantService.deleteMerchant(old));
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/merchants", method = RequestMethod.GET)
	public PageData<Merchant> getTenants(PageLink pageLink) {
		return merchantService.findTenant(getTenantId(), pageLink);
	}
}
