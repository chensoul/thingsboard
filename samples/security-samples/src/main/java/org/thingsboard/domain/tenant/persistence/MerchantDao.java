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
package org.thingsboard.domain.tenant.persistence;

import java.util.Optional;
import org.thingsboard.common.dao.Dao;
import org.thingsboard.common.dao.page.PageData;
import org.thingsboard.common.dao.page.PageLink;
import org.thingsboard.domain.tenant.model.Merchant;

/**
 * The Interface CustomerDao.
 */
public interface MerchantDao extends Dao<Merchant, Long> {
	Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String name);

	void removeByTenantId(String tenantId);

	PageData<Merchant> findTenants(String tenantId, PageLink pageLink);
}
