/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.tenant.service;


import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thingsboard.domain.tenant.model.Merchant;

public interface MerchantService {
	String PUBLIC_CUSTOMER_TITLE = "Public";

	Merchant findMerchantById(Long merchantId);

	Merchant saveMerchant(Merchant merchant);

	Page<Merchant> findTenant(Pageable pageable, String tenantId, String textSearch);

	void deleteMerchant(Merchant merchant);

	void deleteMerchantByTenantId(String tenantId);

	Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String customerName);
}
