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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.audit.model.ActionType;
import org.thingsboard.domain.oauth2.model.OAuth2Registration;
import org.thingsboard.domain.oauth2.service.OAuth2Service;
import org.thingsboard.domain.setting.security.SecuritySettingService;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.server.security.jwt.token.JwtPair;
import static org.thingsboard.server.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.PREV_URI_COOKIE_NAME;
import org.thingsboard.server.security.oauth2.mapper.OAuth2ClientMapper;
import org.thingsboard.server.security.rest.RestAuthenticationDetail;

@Slf4j
@RequiredArgsConstructor
@Component(value = "oauth2AuthenticationSuccessHandler")
public class Oauth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtTokenFactory tokenFactory;
	private final OAuth2ClientMapperProvider oauth2ClientMapperProvider;
	private final OAuth2Service oAuth2Service;
	private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
	private final SecuritySettingService securitySettingService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
										HttpServletResponse response,
										Authentication authentication) throws IOException {
		OAuth2AuthorizationRequest authorizationRequest = httpCookieOAuth2AuthorizationRequestRepository.loadAuthorizationRequest(request);
		String callbackUrlScheme = authorizationRequest.getAttribute(OAuth2ParameterNames.CALLBACK_URL_SCHEME);
		String baseUrl;
		if (!StringUtils.isEmpty(callbackUrlScheme)) {
			baseUrl = callbackUrlScheme + ":";
		} else {
			baseUrl = this.securitySettingService.getBaseUrl(request);
			Optional<Cookie> prevUrlOpt = CookieUtils.getCookie(request, PREV_URI_COOKIE_NAME);
			if (prevUrlOpt.isPresent()) {
				baseUrl += prevUrlOpt.get().getValue();
				CookieUtils.deleteCookie(request, response, PREV_URI_COOKIE_NAME);
			}
		}
		try {
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

			OAuth2Registration registration = oAuth2Service.findRegistration(token.getAuthorizedClientRegistrationId());
			OAuth2AuthorizedClient oAuth2AuthorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(
				token.getAuthorizedClientRegistrationId(),
				token.getPrincipal().getName());
			OAuth2ClientMapper mapper = oauth2ClientMapperProvider.getOAuth2ClientMapperByType(registration.getMapperConfig().getType());
			SecurityUser securityUser = mapper.getOrCreateUserByClientPrincipal(request, token, oAuth2AuthorizedClient.getAccessToken().getTokenValue(),
				registration);

			clearAuthenticationAttributes(request, response);

			JwtPair tokenPair = tokenFactory.createTokenPair(securityUser);
			getRedirectStrategy().sendRedirect(request, response, getRedirectUrl(baseUrl, tokenPair));
			securitySettingService.logLoginAction(securityUser, ActionType.LOGIN, null, new RestAuthenticationDetail(request), registration.getProviderId());
		} catch (Exception e) {
			log.debug("Error occurred during processing authentication success result. " +
					  "request [{}], response [{}], authentication [{}]", request, response, authentication, e);
			clearAuthenticationAttributes(request, response);
			String errorPrefix;
			if (!StringUtils.isEmpty(callbackUrlScheme)) {
				errorPrefix = "/?error=";
			} else {
				errorPrefix = "/login?loginError=";
			}
			getRedirectStrategy().sendRedirect(request, response, baseUrl + errorPrefix +
																  URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
		}
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

	String getRedirectUrl(String baseUrl, JwtPair tokenPair) {
		if (baseUrl.indexOf("?") > 0) {
			baseUrl += "&";
		} else {
			baseUrl += "/?";
		}
		return baseUrl + "accessToken=" + tokenPair.getToken() + "&refreshToken=" + tokenPair.getRefreshToken();
	}
}
