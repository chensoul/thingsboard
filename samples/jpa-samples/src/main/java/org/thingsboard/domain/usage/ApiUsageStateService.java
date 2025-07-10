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
package org.thingsboard.domain.usage;

import java.io.Serializable;
import org.thingsboard.data.dao.EntityDaoService;

public interface ApiUsageStateService extends EntityDaoService<Long> {

	ApiUsageState createDefaultApiUsageState(String id, Serializable entityId);

	ApiUsageState update(ApiUsageState apiUsageState);

	ApiUsageState findTenantApiUsageState(String tenantId);

	ApiUsageState findApiUsageStateByEntityId(Serializable entityId);

	void deleteApiUsageStateByTenantId(String tenantId);

	void deleteApiUsageStateByEntityId(Serializable entityId);

	ApiUsageState findApiUsageStateById(String tenantId, Long id);
}
