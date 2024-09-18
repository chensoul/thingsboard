package com.chensoul.system.domain.usage.limit;

import com.chensoul.system.domain.audit.domain.EntityType;
import lombok.Getter;

public class RateLimitedException extends RuntimeException {
    @Getter
    private final EntityType entityType;

    public RateLimitedException(EntityType entityType) {
        super(entityType + " rate limits reached!");
        this.entityType = entityType;
    }
}
