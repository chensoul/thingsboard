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
