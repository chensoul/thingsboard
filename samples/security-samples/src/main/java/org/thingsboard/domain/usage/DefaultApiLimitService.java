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
package org.thingsboard.domain.usage;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.common.dao.entity.EntityServiceRegistry;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.tenant.model.DefaultTenantProfileConfiguration;
import org.thingsboard.domain.tenant.model.TenantProfile;
import org.thingsboard.domain.tenant.service.TenantProfileService;
import org.thingsboard.server.security.SecurityUtils;

@Service
@RequiredArgsConstructor
public class DefaultApiLimitService implements ApiLimitService {

	private final EntityServiceRegistry entityServiceRegistry;
	private final TenantProfileService tenantProfileService;

	@Override
	public boolean checkEntitiesLimit(String tenantId, EntityType entityType) {
		long limit = getLimit(tenantId, profileConfiguration -> profileConfiguration.getEntitiesLimit(entityType));
		if (limit <= 0) {
			return true;
		}

		long currentCount = entityServiceRegistry.getServiceByEntityType(entityType).countByTenantId(tenantId);
		return currentCount < limit;
	}

	@Override
	public long getLimit(String tenantId, Function<DefaultTenantProfileConfiguration, Number> extractor) {
		if (SecurityUtils.isSysTenantId(tenantId)) {
			return 0L;
		}
		TenantProfile tenantProfile = tenantProfileService.findDefaultTenantProfile();
		if (tenantProfile == null) {
			return 0L;
		}
		Number value = extractor.apply(tenantProfile.getDefaultProfileConfiguration());
		if (value == null) {
			return 0L;
		}
		return Math.max(0, value.longValue());
	}

}
