/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
