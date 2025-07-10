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
package com.chensoul.system.infrastructure.security.oauth2;

import com.chensoul.system.domain.oauth2.domain.OAuth2Registration;
import com.chensoul.system.domain.oauth2.service.OAuth2Service;
import com.chensoul.system.domain.setting.service.SecuritySettingService;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.infrastructure.security.jwt.JwtTokenFactory;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import static com.chensoul.system.infrastructure.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.PREV_URI_COOKIE_NAME;
import com.chensoul.system.infrastructure.security.oauth2.mapper.OAuth2ClientMapper;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final SystemSettingService systemSettingService;

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
            baseUrl = this.systemSettingService.getBaseUrl(request);
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
//            securitySettingService.logLoginAction(securityUser, ActionType.LOGIN, null, new RestAuthenticationDetail(request), registration.getProviderId());
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
