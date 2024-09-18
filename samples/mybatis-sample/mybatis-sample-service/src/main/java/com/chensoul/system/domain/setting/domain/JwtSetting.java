package com.chensoul.system.domain.setting.domain;

import com.chensoul.system.infrastructure.security.jwt.token.JwtToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtSetting {
    public static final String TOKEN_SIGNING_KEY_DEFAULT = "defaultSigningKey";

    /**
     * {@link JwtToken} will expire after this time.
     */
    private Integer tokenExpirationTime = 9000;

    /**
     * {@link JwtToken} can be refreshed during this timeframe.
     */
    private Integer refreshTokenExpTime = 604800;

    /**
     * Token issuer.
     */
    private String tokenIssuer = "chensoul.io";

    /**
     * Key is used to sign {@link JwtToken}.
     * Base64 encoded
     */
    private String tokenSigningKey = TOKEN_SIGNING_KEY_DEFAULT;

    public boolean isSigningKeyDefault() {
        return TOKEN_SIGNING_KEY_DEFAULT.equals(getTokenSigningKey());
    }
}
