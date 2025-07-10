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
package com.chensoul.system.domain.setting.controller;

import static com.chensoul.data.validation.Validators.checkNotNull;
import com.chensoul.exception.BusinessException;
import com.chensoul.json.JacksonUtils;
import static com.chensoul.system.DataConstants.PREV_URI_COOKIE_NAME;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.notification.channel.mail.MailService;
import com.chensoul.system.domain.notification.channel.sms.SmsService;
import com.chensoul.system.domain.notification.channel.sms.TestSmsRequest;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import com.chensoul.system.domain.setting.domain.SystemSettingType;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import static com.chensoul.system.infrastructure.security.SecurityConfiguration.MAIL_OAUTH2_PROCESSING_ENTRY_POINT;
import com.chensoul.system.infrastructure.security.util.CookieUtils;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getCurrentUser;
import com.chensoul.util.RestResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import static org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices.DEFAULT_TOKEN_LENGTH;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@Slf4j
@RequestMapping("/api/systems")
@RequiredArgsConstructor
public class SystemSettingController {
    private static final String PREV_URI_PARAMETER = "prevUri";
    private static final String STATE_COOKIE_NAME = "state";

    private final SmsService smsService;
    private final MailService mailService;
    private final SystemSettingService systemSettingService;

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/setting/{type}")
    public RestResponse<SystemSetting> findSystemSettingByType(@PathVariable("type") SystemSettingType type) {
        SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, type);

        if (systemSetting != null && systemSetting.getType().equals(SystemSettingType.EMAIL)) {
            ((ObjectNode) systemSetting.getExtra()).remove("password");
            ((ObjectNode) systemSetting.getExtra()).remove("refreshToken");
        }
        return RestResponse.ok(systemSetting);
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @PostMapping(value = "/setting")
    public RestResponse<SystemSetting> saveSystemSetting(@RequestBody SystemSetting systemSetting) {
        return RestResponse.ok(systemSettingService.saveSystemSetting(SYS_TENANT_ID, systemSetting));
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @PostMapping(value = "/setting/mail/test")
    public RestResponse sendTestMail(@RequestBody SystemSetting systemSetting) {
        if (systemSetting.getType().equals(SystemSettingType.EMAIL)) {
            mailService.sendTestMail(systemSetting, getCurrentUser().getEmail());
        }
        return RestResponse.ok();
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @PostMapping(value = "/setting/sms/test")
    public RestResponse sendTestSms(@RequestBody TestSmsRequest testSmsRequest) {
        smsService.sendTestSms(testSmsRequest);
        return RestResponse.ok();
    }

//    @PreAuthorize("hasAuthority('SYS_ADMIN')")
//    @RequestMapping(value = "/info", method = RequestMethod.GET)
//    public ServiceInfo getSystemInfo() {
//        return serviceInfoService.getServiceInfo();
//    }

//    @PreAuthorize("hasAuthority('SYS_ADMIN')")
//    @RequestMapping(value = "/feature", method = RequestMethod.GET)
//    public ServiceFeature getFeatureInfo() {
//        return serviceInfoService.getFeatureInfo();
//    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @GetMapping(value = "/setting/mail/oauth2/loginProcessingUrl")
    public String getMailProcessingUrl() {
        return MAIL_OAUTH2_PROCESSING_ENTRY_POINT;
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/setting/mail/oauth2/authorize", method = RequestMethod.GET, produces = "application/text")
    public String getAuthorizationUrl(HttpServletRequest request, HttpServletResponse response) {
        String state = RandomStringUtils.randomAlphabetic(DEFAULT_TOKEN_LENGTH);
        if (request.getParameter(PREV_URI_PARAMETER) != null) {
            CookieUtils.addCookie(response, PREV_URI_COOKIE_NAME, request.getParameter(PREV_URI_PARAMETER), 180);
        }
        CookieUtils.addCookie(response, STATE_COOKIE_NAME, state, 180);

        SystemSetting systemSetting = checkNotNull(systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.EMAIL), "No Administration mail settings found");
        JsonNode jsonValue = systemSetting.getExtra();

        String clientId = checkNotNull(jsonValue.get("clientId"), "No clientId was configured").asText();
        String authUri = checkNotNull(jsonValue.get("authUri"), "No authorization uri was configured").asText();
        String redirectUri = checkNotNull(jsonValue.get("redirectUri"), "No Redirect uri was configured").asText();
        List<String> scope = JacksonUtils.convertValue(checkNotNull(jsonValue.get("scope"), "No scope was configured"), new TypeReference<List<String>>() {
        });

        return new AuthorizationCodeRequestUrl(authUri, clientId).setScopes(scope).setState(state).setRedirectUri(redirectUri).build();
    }

    @GetMapping(value = "/setting/mail/oauth2/code", params = {"code", "state"})
    public void codeProcessingUrl(
        @RequestParam(value = "code") String code, @RequestParam(value = "state") String state,
        HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<Cookie> prevUrlOpt = CookieUtils.getCookie(request, PREV_URI_COOKIE_NAME);
        Optional<Cookie> cookieState = CookieUtils.getCookie(request, STATE_COOKIE_NAME);

        String baseUrl = systemSettingService.getBaseUrl(request);
        String prevUri = baseUrl + (prevUrlOpt.isPresent() ? prevUrlOpt.get().getValue() : "/settings/outgoing-mail");

        if (!cookieState.isPresent() || !cookieState.get().getValue().equals(state)) {
            CookieUtils.deleteCookie(request, response, STATE_COOKIE_NAME);
            throw new BusinessException("Refresh token was not generated, invalid state param");
        }
        CookieUtils.deleteCookie(request, response, STATE_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, PREV_URI_COOKIE_NAME);

        SystemSetting systemSetting = checkNotNull(systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.EMAIL), "No Administration mail settings found");
        JsonNode jsonValue = systemSetting.getExtra();

        String clientId = checkNotNull(jsonValue.get("clientId"), "No clientId was configured").asText();
        String clientSecret = checkNotNull(jsonValue.get("clientSecret"), "No client secret was configured").asText();
        String clientRedirectUri = checkNotNull(jsonValue.get("redirectUri"), "No Redirect uri was configured").asText();
        String tokenUri = checkNotNull(jsonValue.get("tokenUri"), "No authorization uri was configured").asText();

        TokenResponse tokenResponse;
        try {
            tokenResponse = new AuthorizationCodeTokenRequest(new NetHttpTransport(), new GsonFactory(), new GenericUrl(tokenUri), code)
                .setRedirectUri(clientRedirectUri)
                .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
                .execute();
        } catch (IOException e) {
            log.warn("Unable to retrieve refresh token: {}", e.getMessage());
            throw new BusinessException("Error while requesting access token: " + e.getMessage());
        }
        ((ObjectNode) jsonValue).put("refreshToken", tokenResponse.getRefreshToken());
        ((ObjectNode) jsonValue).put("tokenGenerated", true);

        systemSettingService.saveSystemSetting(SYS_TENANT_ID, systemSetting);
        response.sendRedirect(prevUri);
    }
}
