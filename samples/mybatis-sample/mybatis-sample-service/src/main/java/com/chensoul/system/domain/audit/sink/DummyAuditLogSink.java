package com.chensoul.system.domain.audit.sink;

import com.chensoul.system.domain.audit.domain.AuditLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "audit-log.sink", value = "type", havingValue = "none", matchIfMissing = true)
public class DummyAuditLogSink implements AuditLogSink {
    @Override
    public void logAction(AuditLog auditLog) {
    }
}
