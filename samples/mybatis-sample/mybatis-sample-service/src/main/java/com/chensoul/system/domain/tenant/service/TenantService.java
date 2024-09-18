package com.chensoul.system.domain.tenant.service;

import com.chensoul.system.domain.tenant.domain.Tenant;
import com.chensoul.system.domain.tenant.domain.TenantInfo;
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
