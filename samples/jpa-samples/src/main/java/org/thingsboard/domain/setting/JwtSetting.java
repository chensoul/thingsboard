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
package org.thingsboard.domain.setting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.security.jwt.token.JwtToken;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtSetting {
	/**
	 * {@link JwtToken} will expire after this time.
	 */
	private Integer tokenExpirationTime;

	/**
	 * {@link JwtToken} can be refreshed during this timeframe.
	 */
	private Integer refreshTokenExpTime;

	/**
	 * Token issuer.
	 */
	private String tokenIssuer;

	/**
	 * Key is used to sign {@link JwtToken}.
	 * Base64 encoded
	 */
	private String tokenSigningKey;

}
