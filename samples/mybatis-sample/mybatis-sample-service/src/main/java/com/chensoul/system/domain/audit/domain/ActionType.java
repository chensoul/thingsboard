package com.chensoul.system.domain.audit.domain;

import lombok.Getter;

public enum ActionType {

    ADD(false), // log entity
    DELETE(false), // log string id
    UPDATE(false),
    CREDENTIAL_UPDATE(false),
    LOGIN(false),
    LOGOUT(false),
    LOCKOUT(false),
    SMS_SENT(false);

    @Getter
    private final boolean isRead;

    ActionType(boolean isRead) {
        this.isRead = isRead;
    }
}
