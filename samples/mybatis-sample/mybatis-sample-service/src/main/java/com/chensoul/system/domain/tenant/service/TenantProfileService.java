package com.chensoul.system.domain.tenant.service;


import com.chensoul.system.domain.tenant.domain.TenantProfile;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;

public interface TenantProfileService {
    TenantProfile findTenantProfileById(Long tenantProfileId);

    TenantProfile findTenantProfileByTenantId(String tenantId);

    TenantProfile findOrCreateDefaultTenantProfile();

    TenantProfile saveTenantProfile(TenantProfile tenantProfile);

    TenantProfile findDefaultTenantProfile();

    void deleteTenantProfile(TenantProfile tenantProfileId);

    TenantProfile setDefaultTenantProfile(TenantProfile tenantProfile);

    PageData<TenantProfile> findTenantProfiles(PageLink pageLink);
}
