package com.chensoul.system.domain.audit.service;

import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.audit.domain.AuditLog;
import com.chensoul.validation.DataValidator;
import org.springframework.stereotype.Component;

@Component
public class AuditLogValidator extends DataValidator<AuditLog> {

    @Override
    protected void validateDataImpl(AuditLog auditLog) {
        if (auditLog.getTenantId() == null) {
            throw new BusinessException("Tenant Id should be specified!");
        }
        if (auditLog.getUserId() == null) {
            throw new BusinessException("User Id should be specified!");
        }
    }
}
