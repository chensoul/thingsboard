package com.chensoul.system.domain.tenant.service;

import com.chensoul.system.domain.tenant.domain.TenantProfile;
import com.chensoul.system.domain.tenant.mybatis.TenantProfileDao;
import com.chensoul.exception.BusinessException;
import com.chensoul.validation.DataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class TenantProfileValidator extends DataValidator<TenantProfile> {

    @Autowired
    private TenantProfileDao tenantProfileDao;

    @Autowired
    @Lazy
    private TenantProfileService tenantProfileService;

    @Override
    protected void validateDataImpl(TenantProfile tenantProfile) {
        if (tenantProfile.getExtra() == null) {
            throw new BusinessException("Tenant profile extra should be specified");
        }

        if (tenantProfile.isDefaulted()) {
            TenantProfile defaultTenantProfile = tenantProfileService.findDefaultTenantProfile();
            if (defaultTenantProfile != null && !defaultTenantProfile.getId().equals(tenantProfile.getId())) {
                throw new BusinessException("Another default tenant profile is present");
            }
        }
    }

    @Override
    protected TenantProfile validateUpdate(TenantProfile tenantProfile) {
        TenantProfile old = tenantProfileDao.findById(tenantProfile.getId());
        if (old == null) {
            throw new BusinessException("Can't update non existing tenant profile");
        }
        return old;
    }
}
