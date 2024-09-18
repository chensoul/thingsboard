package com.chensoul.system.infrastructure.security.util;

import java.io.Serializable;
import lombok.Getter;

@Getter
public class UserPrincipal implements Serializable {
    private final Type type;
    private final String value;

    public UserPrincipal(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public enum Type {
        USER_NAME,
        PUBLIC_ID
    }

}
