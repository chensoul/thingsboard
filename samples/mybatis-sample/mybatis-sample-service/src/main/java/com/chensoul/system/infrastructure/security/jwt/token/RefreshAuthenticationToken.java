package com.chensoul.system.infrastructure.security.jwt.token;


import com.chensoul.system.infrastructure.security.util.SecurityUser;

public class RefreshAuthenticationToken extends AbstractJwtAuthenticationToken {

    private static final long serialVersionUID = -1311042791508924523L;

    public RefreshAuthenticationToken(RawAccessJwtToken unsafeToken) {
        super(unsafeToken);
    }

    public RefreshAuthenticationToken(SecurityUser securityUser) {
        super(securityUser);
    }
}
