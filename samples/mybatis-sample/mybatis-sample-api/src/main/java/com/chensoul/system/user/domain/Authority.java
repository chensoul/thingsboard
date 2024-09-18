package com.chensoul.system.user.domain;

public enum Authority {
    SYS_ADMIN,
    TENANT_ADMIN,
    MERCHANT_USER,
    REFRESH_TOKEN,
    PRE_VERIFICATION_TOKEN;

    public static Authority parse(String value) {
        Authority authority = null;
        if (value != null && value.length() != 0) {
            for (Authority current : Authority.values()) {
                if (current.name().equalsIgnoreCase(value)) {
                    authority = current;
                    break;
                }
            }
        }
        return authority;
    }
}
