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


import java.util.Optional;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;

public interface MerchantService {
	String PUBLIC_CUSTOMER_TITLE = "Public";

	Merchant findMerchantById(Long merchantId);

	Merchant saveMerchant(Merchant merchant);

	PageData<Merchant> findTenant(String tenantId, PageLink pageLink);

	void deleteMerchant(Merchant merchant);

	void deleteMerchantByTenantId(String tenantId);

	Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String customerName);
}
