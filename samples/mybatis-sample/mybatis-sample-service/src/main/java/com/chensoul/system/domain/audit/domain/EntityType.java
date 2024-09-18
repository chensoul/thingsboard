package com.chensoul.system.domain.audit.domain;

import lombok.Getter;

@Getter
public enum EntityType {
    TENANT("租户"),
    TENANT_PROFILE("租户配置"),
    MERCHANT("商户"),
    USER("用户"),
    USER_SETTING("用户配置"),
    ROLE("角色"),
    NOTIFICATION_TARGET,
    NOTIFICATION_TEMPLATE,
    NOTIFICATION_REQUEST,
    NOTIFICATION_RULE;

    private String name;

    private EntityType() {
    }

    EntityType(String name) {
        this.name = name;
    }
}
