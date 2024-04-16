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
package org.thingsboard.domain.setting.jwt;

import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thingsboard.common.exception.DataValidationException;
import static org.thingsboard.domain.setting.jwt.DefaultJwtSettingService.TOKEN_SIGNING_KEY_DEFAULT;

@Component
public class JwtSettingValidator {
	public void validate(JwtSetting jwtSetting) {
		if (StringUtils.isEmpty(jwtSetting.getTokenIssuer())) {
			throw new DataValidationException("JWT token issuer should be specified!");
		}
		if (Optional.ofNullable(jwtSetting.getRefreshTokenExpTime()).orElse(0) < TimeUnit.MINUTES.toSeconds(15)) {
			throw new DataValidationException("JWT refresh token expiration time should be at least 15 minutes!");
		}
		if (Optional.ofNullable(jwtSetting.getTokenExpirationTime()).orElse(0) < TimeUnit.MINUTES.toSeconds(1)) {
			throw new DataValidationException("JWT token expiration time should be at least 1 minute!");
		}
		if (jwtSetting.getTokenExpirationTime() >= jwtSetting.getRefreshTokenExpTime()) {
			throw new DataValidationException("JWT token expiration time should greater than JWT refresh token expiration time!");
		}
		if (StringUtils.isEmpty(jwtSetting.getTokenSigningKey())) {
			throw new DataValidationException("JWT token signing key should be specified!");
		}

		byte[] decodedKey;
		try {
			decodedKey = Base64.getDecoder().decode(jwtSetting.getTokenSigningKey());
		} catch (Exception e) {
			throw new DataValidationException("JWT token signing key should be a valid Base64 encoded string! " + e.getMessage());
		}

		if (ArrayUtils.isEmpty(decodedKey)) {
			throw new DataValidationException("JWT token signing key should be non-empty after Base64 decoding!");
		}
		if (decodedKey.length * Byte.SIZE < 256 && !TOKEN_SIGNING_KEY_DEFAULT.equals(jwtSetting.getTokenSigningKey())) {
			throw new DataValidationException("JWT token signing key should be a Base64 encoded string representing at least 256 bits of data!");
		}

		System.arraycopy(decodedKey, 0, RandomUtils.nextBytes(decodedKey.length), 0, decodedKey.length); //secure memory
	}

}
