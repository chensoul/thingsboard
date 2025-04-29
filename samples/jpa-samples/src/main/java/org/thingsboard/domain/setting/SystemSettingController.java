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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.BaseController;
import org.thingsboard.common.util.JacksonUtil;
import static org.thingsboard.common.validation.Validator.checkNotNull;
import org.thingsboard.domain.notification.internal.channel.mail.MailService;
import static org.thingsboard.server.config.SecurityConfiguration.MAIL_OAUTH2_PROCESSING_ENTRY_POINT;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.server.security.oauth2.CookieUtils;
import static org.thingsboard.server.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.PREV_URI_COOKIE_NAME;
import org.thingsboard.server.security.permission.AccessControlService;
import org.thingsboard.server.security.permission.Operation;
import org.thingsboard.server.security.permission.Resource;
import org.thingsboard.domain.notification.internal.channel.sms.SmsService;
import org.thingsboard.domain.notification.internal.channel.sms.TestSmsRequest;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@Slf4j
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemSettingController extends BaseController {
	private static final String PREV_URI_PARAMETER = "prevUri";
	private static final String STATE_COOKIE_NAME = "state";

	private final SmsService smsService;
	private final MailService mailService;
	private final SystemSettingService systemSettingService;
	private final SecuritySettingService securitySettingService;
	private final JwtSettingService jwtSettingService;
	private final JwtTokenFactory tokenFactory;
	private final AccessControlService accessControlService;
	private final ServiceInfoService serviceInfoService;

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@GetMapping(value = "/setting/{type}")
	public SystemSetting getSystemSetting(@PathVariable("type") String type) {
		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.WRITE);

		SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.valueOf(type.toUpperCase()));
		if (systemSetting != null && systemSetting.getType().equals(SystemSettingType.EMAIL)) {
			((ObjectNode) systemSetting.getExtra()).remove("password");
			((ObjectNode) systemSetting.getExtra()).remove("refreshToken");
		}
		return systemSetting;
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@PostMapping(value = "/setting")
	public SystemSetting saveSystemSetting(@RequestBody SystemSetting systemSetting) {
		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.WRITE);

		return systemSettingService.saveSystemSetting(SYS_TENANT_ID, systemSetting);
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@GetMapping(value = "/securitySetting")
	public SecuritySetting getSecuritySettings() {
		return securitySettingService.getSecuritySetting();
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@PostMapping(value = "/securitySetting")
	public SecuritySetting saveSecuritySettings(@RequestBody SecuritySetting securitySetting) {
		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.WRITE);

		return securitySettingService.saveSecuritySetting(securitySetting);
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@GetMapping(value = "/jwtSetting")
	public JwtSetting getJwtSettings() {
		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.READ);

		return jwtSettingService.getJwtSetting();
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@PostMapping(value = "/jwtSetting")
	public JwtPair saveJwtSetting(@RequestBody JwtSetting jwtSetting) {
		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.WRITE);

		jwtSettingService.saveJwtSetting(jwtSetting);
		return tokenFactory.createTokenPair(getCurrentUser());
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@PostMapping(value = "/mailSetting/test")
	public void sendTestMail(@RequestBody SystemSetting systemSetting) {
		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.READ);

		if (systemSetting.getType().equals(SystemSettingType.EMAIL)) {
			mailService.sendTestMail(systemSetting, getCurrentUser().getEmail());
		}
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@PostMapping(value = "/smsSetting/test")
	public void sendTestSms(@RequestBody TestSmsRequest testSmsRequest) {
		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.READ);
		smsService.sendTestSms(testSmsRequest);
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ServiceInfo getSystemInfo() throws ThingsboardException {
		return serviceInfoService.getServiceInfo();
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/feature", method = RequestMethod.GET)
	public ServiceFeature getFeatureInfo() {
		return serviceInfoService.getFeatureInfo();
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/mail/oauth2/loginProcessingUrl", method = RequestMethod.GET)
	public String getMailProcessingUrl() throws ThingsboardException {
		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.READ);
		return MAIL_OAUTH2_PROCESSING_ENTRY_POINT;
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/mail/oauth2/authorize", method = RequestMethod.GET, produces = "application/text")
	public String getAuthorizationUrl(HttpServletRequest request, HttpServletResponse response) throws ThingsboardException {
		String state = RandomStringUtils.randomAlphabetic(DEFAULT_TOKEN_LENGTH);
		if (request.getParameter(PREV_URI_PARAMETER) != null) {
			CookieUtils.addCookie(response, PREV_URI_COOKIE_NAME, request.getParameter(PREV_URI_PARAMETER), 180);
		}
		CookieUtils.addCookie(response, STATE_COOKIE_NAME, state, 180);

		accessControlService.checkPermission(getCurrentUser(), Resource.SYSTEM_SETTING, Operation.READ);
		SystemSetting systemSetting = checkNotNull(systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.EMAIL), "No Administration mail settings found");
		JsonNode jsonValue = systemSetting.getExtra();

		String clientId = checkNotNull(jsonValue.get("clientId"), "No clientId was configured").asText();
		String authUri = checkNotNull(jsonValue.get("authUri"), "No authorization uri was configured").asText();
		String redirectUri = checkNotNull(jsonValue.get("redirectUri"), "No Redirect uri was configured").asText();
		List<String> scope = JacksonUtil.convertValue(checkNotNull(jsonValue.get("scope"), "No scope was configured"), new TypeReference<>() {
		});

		return new AuthorizationCodeRequestUrl(authUri, clientId).setScopes(scope).setState(state).setRedirectUri(redirectUri).build();
	}

	@RequestMapping(value = "/mail/oauth2/code", params = {"code", "state"}, method = RequestMethod.GET)
	public void codeProcessingUrl(
		@RequestParam(value = "code") String code, @RequestParam(value = "state") String state,
		HttpServletRequest request, HttpServletResponse response) throws ThingsboardException, IOException {
		Optional<Cookie> prevUrlOpt = CookieUtils.getCookie(request, PREV_URI_COOKIE_NAME);
		Optional<Cookie> cookieState = CookieUtils.getCookie(request, STATE_COOKIE_NAME);

		String baseUrl = securitySettingService.getBaseUrl(request);
		String prevUri = baseUrl + (prevUrlOpt.isPresent() ? prevUrlOpt.get().getValue() : "/settings/outgoing-mail");

		if (cookieState.isEmpty() || !cookieState.get().getValue().equals(state)) {
			CookieUtils.deleteCookie(request, response, STATE_COOKIE_NAME);
			throw new ThingsboardException("Refresh token was not generated, invalid state param", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
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
			throw new ThingsboardException("Error while requesting access token: " + e.getMessage(), ThingsboardErrorCode.GENERAL);
		}
		((ObjectNode) jsonValue).put("refreshToken", tokenResponse.getRefreshToken());
		((ObjectNode) jsonValue).put("tokenGenerated", true);

		systemSettingService.saveSystemSetting(SYS_TENANT_ID, systemSetting);
		response.sendRedirect(prevUri);
	}
}
