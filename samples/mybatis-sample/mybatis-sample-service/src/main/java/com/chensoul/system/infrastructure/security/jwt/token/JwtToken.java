package com.chensoul.system.infrastructure.security.jwt.token;

import java.io.Serializable;

public interface JwtToken extends Serializable {
    String getToken();
}
