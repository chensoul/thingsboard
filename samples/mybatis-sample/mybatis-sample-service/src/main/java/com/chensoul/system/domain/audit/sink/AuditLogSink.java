package com.chensoul.system.domain.audit.sink;

import com.chensoul.system.domain.audit.domain.AuditLog;

public interface AuditLogSink {

    void logAction(AuditLog auditLog);
}
