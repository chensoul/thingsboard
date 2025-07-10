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
package org.thingsboard.server.security.jwt.extractor;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component(value = "jwtHeaderTokenExtractor")
public class JwtHeaderTokenExtractor implements TokenExtractor {
	public static final String HEADER_PREFIX = "Bearer ";
	public static final String REQUEST_PREFIX = "accessToken";

	@Override
	public String extract(HttpServletRequest request) {
		String header = request.getHeader(JWT_TOKEN_HEADER_PARAM);
		if (StringUtils.isNotBlank(header)) {
			if (header.length() < HEADER_PREFIX.length()) {
				throw new AuthenticationServiceException("Invalid authorization header size.");
			}
			header = header.substring(HEADER_PREFIX.length(), header.length());
		} else {
			header = request.getParameter(REQUEST_PREFIX);
		}

		if (StringUtils.isBlank(header)) {
			throw new AuthenticationServiceException("Authorization header cannot be blank!");
		}
		return header;
	}
}
