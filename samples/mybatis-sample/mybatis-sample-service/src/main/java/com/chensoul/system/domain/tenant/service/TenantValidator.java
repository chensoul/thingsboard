package com.chensoul.system.domain.tenant.service;

import com.chensoul.system.domain.tenant.domain.Tenant;
import com.chensoul.system.domain.tenant.mybatis.TenantDao;
import com.chensoul.exception.BusinessException;
import com.chensoul.validation.DataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TenantValidator extends DataValidator<Tenant> {

    @Autowired
    private TenantDao tenantDao;

    @Override
    protected void validateDataImpl(Tenant tenant) {
    }

    @Override
    protected Tenant validateUpdate(Tenant tenant) {
        Tenant old = tenantDao.findById(tenant.getId());
        if (old == null) {
            throw new BusinessException("Can't update non existing tenant!");
        }
        return old;
    }
}
