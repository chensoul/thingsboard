package com.chensoul.system.domain.usage;

import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.tenant.domain.DefaultTenantProfileConfiguration;
import java.util.function.Function;

public interface ApiLimitService {

    boolean checkEntitiesLimit(String tenantId, EntityType entityType);

    long getLimit(String tenantId, Function<DefaultTenantProfileConfiguration, Number> extractor);

}
