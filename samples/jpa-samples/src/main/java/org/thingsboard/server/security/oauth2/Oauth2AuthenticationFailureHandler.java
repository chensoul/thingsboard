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
package org.thingsboard.server.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.setting.SecuritySettingService;

@Component(value = "oauth2AuthenticationFailureHandler")
public class Oauth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
	private final SecuritySettingService securitySettingService;

	@Autowired
	public Oauth2AuthenticationFailureHandler(final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository,
											  final SecuritySettingService securitySettingService) {
		this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
		this.securitySettingService = securitySettingService;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
										HttpServletResponse response, AuthenticationException exception)
		throws IOException, ServletException {
		String baseUrl;
		String errorPrefix;
		String callbackUrlScheme = null;
		OAuth2AuthorizationRequest authorizationRequest = httpCookieOAuth2AuthorizationRequestRepository.loadAuthorizationRequest(request);
		if (authorizationRequest != null) {
			callbackUrlScheme = authorizationRequest.getAttribute(OAuth2ParameterNames.CALLBACK_URL_SCHEME);
		}
		if (!StringUtils.isEmpty(callbackUrlScheme)) {
			baseUrl = callbackUrlScheme + ":";
			errorPrefix = "/?error=";
		} else {
			baseUrl = this.securitySettingService.getBaseUrl(request);
			errorPrefix = "/login?loginError=";
		}
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
		getRedirectStrategy().sendRedirect(request, response, baseUrl + errorPrefix +
															  URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8.toString()));
	}
}
