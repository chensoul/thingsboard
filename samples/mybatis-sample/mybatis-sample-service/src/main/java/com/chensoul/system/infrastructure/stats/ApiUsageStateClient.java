package com.chensoul.system.infrastructure.stats;


import com.chensoul.system.domain.usage.ApiUsageState;

public interface ApiUsageStateClient {

    ApiUsageState getApiUsageState(String tenantId);

}
