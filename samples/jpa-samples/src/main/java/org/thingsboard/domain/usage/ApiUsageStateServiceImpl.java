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
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasId;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
public class ApiUsageStateServiceImpl implements ApiUsageStateService {
	@Override
	public ApiUsageState createDefaultApiUsageState(String id, Serializable entityId) {
		return null;
	}

	@Override
	public ApiUsageState update(ApiUsageState apiUsageState) {
		return null;
	}

	@Override
	public ApiUsageState findTenantApiUsageState(String tenantId) {
		return null;
	}

	@Override
	public ApiUsageState findApiUsageStateByEntityId(Serializable entityId) {
		return null;
	}

	@Override
	public void deleteApiUsageStateByTenantId(String tenantId) {

	}

	@Override
	public void deleteApiUsageStateByEntityId(Serializable entityId) {

	}

	@Override
	public ApiUsageState findApiUsageStateById(String tenantId, Long id) {
		return null;
	}

	@Override
	public Optional<HasId<Long>> findEntity(Long id) {
		return Optional.empty();
	}

	@Override
	public EntityType getEntityType() {
		return null;
	}
}
