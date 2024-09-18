package com.chensoul.system.domain.audit.service.event;

import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.EntityType;
import lombok.Data;

@Data
public class AuditLogEvent<T> {
    private final T entity;
    private final EntityType entityType;
    private final ActionType actionType;

    private Long costTime;

    public AuditLogEvent(T entity, EntityType entityType, ActionType actionType) {
        this.entity = entity;
        this.entityType = entityType;
        this.actionType = actionType;
    }
}
