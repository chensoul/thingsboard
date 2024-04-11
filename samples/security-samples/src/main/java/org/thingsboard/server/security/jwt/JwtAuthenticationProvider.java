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
package org.thingsboard.server.security.jwt;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.jwt.token.JwtAuthenticationToken;
import org.thingsboard.server.security.jwt.token.RawAccessJwtToken;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

	private final JwtTokenFactory tokenFactory;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		RawAccessJwtToken rawAccessToken = (RawAccessJwtToken) authentication.getCredentials();
		SecurityUser securityUser = authenticate(rawAccessToken.getToken());
		return new JwtAuthenticationToken(securityUser);
	}

	public SecurityUser authenticate(String accessToken) throws AuthenticationException {
		if (StringUtils.isEmpty(accessToken)) {
			throw new BadCredentialsException("Token is invalid");
		}
		return tokenFactory.parseAccessJwtToken(accessToken);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
