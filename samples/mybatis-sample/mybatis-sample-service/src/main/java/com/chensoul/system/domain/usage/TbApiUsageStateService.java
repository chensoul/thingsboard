package com.chensoul.system.domain.usage;


import com.chensoul.system.infrastructure.stats.ApiUsageStateClient;

public interface TbApiUsageStateService extends ApiUsageStateClient {

//	void process(TbProtoQueueMsg<ToUsageStatsServiceMsg> msg, TbCallback callback);

    void onTenantProfileUpdate(Long tenantProfileId);

    void onTenantUpdate(String tenantId);

    void onTenantDelete(String tenantId);

    void onCustomerDelete(Long customerId);

    void onApiUsageStateUpdate(String tenantId);
}
