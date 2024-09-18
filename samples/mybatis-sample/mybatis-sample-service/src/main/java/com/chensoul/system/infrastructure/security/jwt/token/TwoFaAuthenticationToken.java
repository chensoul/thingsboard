package com.chensoul.system.infrastructure.security.jwt.token;

import com.chensoul.system.infrastructure.security.util.SecurityUser;

public class TwoFaAuthenticationToken extends AbstractJwtAuthenticationToken {
    public TwoFaAuthenticationToken(SecurityUser securityUser) {
        super(securityUser);
    }
}
