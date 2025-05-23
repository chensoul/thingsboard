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
package org.thingsboard.domain.tenant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantService {

	Tenant findTenantById(String tenantId);

	Tenant findTenantByName(String name);

	TenantInfo findTenantInfoById(String tenantId);

	Tenant saveTenant(Tenant tenant);

	boolean tenantExists(String tenantId);

	void deleteTenant(Tenant tenant);

	Page<Tenant> findTenants(Pageable pageable, String textSearch);
}
