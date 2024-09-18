package com.chensoul.system.domain.usage;

import java.io.Serializable;

public interface ApiUsageStateService {

    ApiUsageState createDefaultApiUsageState(String id, Serializable entityId);

    ApiUsageState update(ApiUsageState apiUsageState);

    ApiUsageState findTenantApiUsageState(String tenantId);

    ApiUsageState findApiUsageStateByEntityId(Serializable entityId);

    void deleteApiUsageStateByTenantId(String tenantId);

    void deleteApiUsageStateByEntityId(Serializable entityId);

    ApiUsageState findApiUsageStateById(String tenantId, Long id);
}
