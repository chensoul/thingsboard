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
package com.chensoul.system.domain.usage;

import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.tenant.domain.DefaultTenantProfileConfiguration;
import com.chensoul.system.domain.tenant.domain.TenantProfile;
import com.chensoul.system.domain.tenant.service.TenantProfileService;
import com.chensoul.system.infrastructure.security.util.SecurityUtils;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultApiLimitService implements ApiLimitService {

    //    private final EntityServiceRegistry entityServiceRegistry;
    private final TenantProfileService tenantProfileService;

    @Override
    public boolean checkEntitiesLimit(String tenantId, EntityType entityType) {
        long limit = getLimit(tenantId, profileConfiguration -> profileConfiguration.getEntitiesLimit(entityType));
        if (limit <= 0) {
            return true;
        }

        long currentCount = 0;// entityServiceRegistry.getServiceByEntityType(entityType).countByTenantId(tenantId);
        return currentCount < limit;
    }

    @Override
    public long getLimit(String tenantId, Function<DefaultTenantProfileConfiguration, Number> extractor) {
        if (SecurityUtils.isSysTenantId(tenantId)) {
            return 0L;
        }
        TenantProfile tenantProfile = tenantProfileService.findTenantProfileByTenantId(tenantId);
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
