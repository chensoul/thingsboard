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
package org.thingsboard.domain.oauth2;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.thingsboard.common.model.BaseData;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

public class OAuth2Utils {
	public static final String OAUTH2_AUTHORIZATION_PATH_TEMPLATE = "/oauth2/authorization/%s";

	public static OAuth2ClientInfo toClientInfo(OAuth2Registration registration) {
		OAuth2ClientInfo client = new OAuth2ClientInfo();
		client.setName(registration.getLoginButtonLabel());
		client.setUrl(String.format(OAUTH2_AUTHORIZATION_PATH_TEMPLATE, registration.getId()));
		client.setIcon(registration.getLoginButtonIcon());
		return client;
	}

	public static OAuth2ParamInfo toOAuth2ParamInfo(List<OAuth2Registration> registrations, List<OAuth2Domain> domains, List<OAuth2Mobile> mobiles) {
		OAuth2ParamInfo oauth2ParamInfo = new OAuth2ParamInfo();
		oauth2ParamInfo.setClientRegistrations(registrations.stream().sorted(Comparator.comparing(BaseData::getId)).map(OAuth2Utils::toOAuth2RegistrationInfo).collect(Collectors.toList()));
		oauth2ParamInfo.setDomainInfos(domains.stream().sorted(Comparator.comparing(BaseData::getId)).map(OAuth2Utils::toOAuth2DomainInfo).collect(Collectors.toList()));
		oauth2ParamInfo.setMobileInfos(mobiles.stream().sorted(Comparator.comparing(BaseData::getId)).map(OAuth2Utils::toOAuth2MobileInfo).collect(Collectors.toList()));
		return oauth2ParamInfo;
	}

	public static OAuth2RegistrationInfo toOAuth2RegistrationInfo(OAuth2Registration registration) {
		return OAuth2RegistrationInfo.builder()
			.mapperConfig(registration.getMapperConfig())
			.clientId(registration.getClientId())
			.clientSecret(registration.getClientSecret())
			.authorizationUri(registration.getAuthorizationUri())
			.accessTokenUri(registration.getAccessTokenUri())
			.scope(registration.getScope())
			.platforms(registration.getPlatforms())
			.userInfoUri(registration.getUserInfoUri())
			.userNameAttributeName(registration.getUserNameAttributeName())
			.jwkSetUri(registration.getJwkSetUri())
			.clientAuthenticationMethod(registration.getClientAuthenticationMethod())
			.loginButtonLabel(registration.getLoginButtonLabel())
			.loginButtonIcon(registration.getLoginButtonIcon())
			.extra(registration.getExtra())
			.build();
	}

	public static OAuth2DomainInfo toOAuth2DomainInfo(OAuth2Domain domain) {
		return OAuth2DomainInfo.builder()
			.name(domain.getDomainName())
			.scheme(domain.getDomainScheme())
			.build();
	}

	public static OAuth2MobileInfo toOAuth2MobileInfo(OAuth2Mobile mobile) {
		return OAuth2MobileInfo.builder()
			.pkgName(mobile.getPkgName())
			.appSecret(mobile.getAppSecret())
			.build();
	}

	public static OAuth2Param infoToOAuth2Param(OAuth2Info oauth2Info) {
		OAuth2Param oauth2Param = new OAuth2Param();
		oauth2Param.setEnabled(oauth2Info.isEnabled());
		oauth2Param.setTenantId(SYS_TENANT_ID);
		return oauth2Param;
	}

	public static OAuth2Registration toOAuth2Registration(Long oauth2ParamId, OAuth2RegistrationInfo registrationInfo) {
		OAuth2Registration registration = new OAuth2Registration();
		registration.setOauth2ParamId(oauth2ParamId);
		registration.setMapperConfig(registrationInfo.getMapperConfig());
		registration.setClientId(registrationInfo.getClientId());
		registration.setClientSecret(registrationInfo.getClientSecret());
		registration.setAuthorizationUri(registrationInfo.getAuthorizationUri());
		registration.setAccessTokenUri(registrationInfo.getAccessTokenUri());
		registration.setScope(registrationInfo.getScope());
		registration.setPlatforms(registrationInfo.getPlatforms());
		registration.setUserInfoUri(registrationInfo.getUserInfoUri());
		registration.setUserNameAttributeName(registrationInfo.getUserNameAttributeName());
		registration.setJwkSetUri(registrationInfo.getJwkSetUri());
		registration.setClientAuthenticationMethod(registrationInfo.getClientAuthenticationMethod());
		registration.setLoginButtonLabel(registrationInfo.getLoginButtonLabel());
		registration.setLoginButtonIcon(registrationInfo.getLoginButtonIcon());
		registration.setExtra(registrationInfo.getExtra());
		return registration;
	}

	public static OAuth2Domain toOAuth2Domain(Long oauth2ParamId, OAuth2DomainInfo domainInfo) {
		OAuth2Domain domain = new OAuth2Domain();
		domain.setOauth2ParamId(oauth2ParamId);
		domain.setDomainName(domainInfo.getName());
		domain.setDomainScheme(domainInfo.getScheme());
		return domain;
	}

	public static OAuth2Mobile toOAuth2Mobile(Long oauth2ParamId, OAuth2MobileInfo mobileInfo) {
		OAuth2Mobile mobile = new OAuth2Mobile();
		mobile.setOauth2ParamId(oauth2ParamId);
		mobile.setPkgName(mobileInfo.getPkgName());
		mobile.setAppSecret(mobileInfo.getAppSecret());
		return mobile;
	}
}
