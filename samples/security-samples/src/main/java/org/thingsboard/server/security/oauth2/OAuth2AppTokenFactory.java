/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.security.oauth2;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OAuth2AppTokenFactory {

	private static final String CALLBACK_URL_SCHEME = "callbackUrlScheme";

	private static final long MAX_EXPIRATION_TIME_DIFF_MS = TimeUnit.MINUTES.toMillis(5);

	public String validateTokenAndGetCallbackUrlScheme(String appPackage, String appToken, String appSecret) {
		Jws<Claims> jwsClaims;
		try {
			jwsClaims = Jwts.parser().setSigningKey(appSecret).parseClaimsJws(appToken);
		} catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException ex) {
			throw new IllegalArgumentException("Invalid Application token: ", ex);
		} catch (ExpiredJwtException expiredEx) {
			throw new IllegalArgumentException("Application token expired", expiredEx);
		}
		Claims claims = jwsClaims.getBody();
		Date expiration = claims.getExpiration();
		if (expiration == null) {
			throw new IllegalArgumentException("Application token must have expiration date");
		}
		long timeDiff = expiration.getTime() - System.currentTimeMillis();
		if (timeDiff > MAX_EXPIRATION_TIME_DIFF_MS) {
			throw new IllegalArgumentException("Application token expiration time can't be longer than 5 minutes");
		}
		if (!claims.getIssuer().equals(appPackage)) {
			throw new IllegalArgumentException("Application token issuer doesn't match application package");
		}
		String callbackUrlScheme = claims.get(CALLBACK_URL_SCHEME, String.class);
		if (StringUtils.isEmpty(callbackUrlScheme)) {
			throw new IllegalArgumentException("Application token doesn't have callbackUrlScheme");
		}
		return callbackUrlScheme;
	}

}
