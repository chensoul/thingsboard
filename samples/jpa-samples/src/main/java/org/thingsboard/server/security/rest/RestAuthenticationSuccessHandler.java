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
package org.thingsboard.server.security.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.setting.TwoFaSettingService;
import org.thingsboard.domain.user.Authority;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.server.security.jwt.token.TwoFaAuthenticationToken;

@RequiredArgsConstructor
@Component(value = "defaultAuthenticationSuccessHandler")
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private final JwtTokenFactory tokenFactory;
	private final TwoFaSettingService twoFaSettingService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) throws IOException, ServletException {
		SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

		JwtPair tokenPair;
		if (authentication instanceof TwoFaAuthenticationToken) {
			int preVerificationTokenLifetime = twoFaSettingService.getTwoFaSystemSetting(true)
				.flatMap(setting -> Optional.ofNullable(setting.getTotalAllowedTimeForVerification())
					.filter(time -> time > 0))
				.orElse((int) TimeUnit.MINUTES.toSeconds(30));
			tokenPair = new JwtPair();
			tokenPair.setToken(tokenFactory.createPreVerificationToken(securityUser, preVerificationTokenLifetime).getToken());
			tokenPair.setScope(Authority.PRE_VERIFICATION_TOKEN);
		} else {
			tokenPair = tokenFactory.createTokenPair(securityUser);
		}

		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		JacksonUtil.writeValue(response.getWriter(), tokenPair);

		clearAuthenticationAttributes(request);
	}

	/**
	 * Removes temporary authentication-related data which may have been stored
	 * in the session during the authentication process..
	 */
	protected final void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}

		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}
}
