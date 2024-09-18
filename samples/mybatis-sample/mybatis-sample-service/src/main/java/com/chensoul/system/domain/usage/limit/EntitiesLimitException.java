package com.chensoul.system.domain.usage.limit;

import com.chensoul.system.domain.audit.domain.EntityType;
import lombok.Getter;

public class EntitiesLimitException extends RuntimeException {
    private static final long serialVersionUID = -9211462514373279196L;

    @Getter
    private final String tenantId;
    @Getter
    private final EntityType entityType;

    public EntitiesLimitException(String tenantId, EntityType entityType) {
        super(entityType.name() + "s limit reached");
        this.tenantId = tenantId;
        this.entityType = entityType;
    }
}
