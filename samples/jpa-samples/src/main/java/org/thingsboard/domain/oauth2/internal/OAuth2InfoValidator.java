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
package org.thingsboard.domain.oauth2.internal;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.domain.oauth2.MapperType;
import org.thingsboard.domain.oauth2.OAuth2BasicMapperConfig;
import org.thingsboard.domain.oauth2.OAuth2CustomMapperConfig;
import org.thingsboard.domain.oauth2.OAuth2DomainInfo;
import org.thingsboard.domain.oauth2.OAuth2Info;
import org.thingsboard.domain.oauth2.OAuth2MapperConfig;
import org.thingsboard.domain.oauth2.OAuth2MobileInfo;
import org.thingsboard.domain.oauth2.OAuth2ParamInfo;
import org.thingsboard.domain.oauth2.OAuth2RegistrationInfo;
import org.thingsboard.domain.oauth2.SchemeType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class OAuth2InfoValidator implements Consumer<OAuth2Info> {
	@Override
	public void accept(OAuth2Info oauth2Info) {
		if (oauth2Info == null
			|| oauth2Info.getOauth2ParamInfos() == null) {
			throw new DataValidationException("OAuth2 param infos should be specified!");
		}
		for (OAuth2ParamInfo oauth2Params : oauth2Info.getOauth2ParamInfos()) {
			if (oauth2Params.getDomainInfos() == null
				|| oauth2Params.getDomainInfos().isEmpty()) {
				throw new DataValidationException("List of domain configuration should be specified!");
			}
			for (OAuth2DomainInfo domainInfo : oauth2Params.getDomainInfos()) {
				if (StringUtils.isEmpty(domainInfo.getName())) {
					throw new DataValidationException("Domain name should be specified!");
				}
				if (domainInfo.getScheme() == null) {
					throw new DataValidationException("Domain scheme should be specified!");
				}
			}
			oauth2Params.getDomainInfos().stream()
				.collect(Collectors.groupingBy(OAuth2DomainInfo::getName))
				.forEach((domainName, domainInfos) -> {
					if (domainInfos.size() > 1 && domainInfos.stream().anyMatch(domainInfo -> domainInfo.getScheme() == SchemeType.MIXED)) {
						throw new DataValidationException("MIXED scheme type shouldn't be combined with another scheme type!");
					}
					domainInfos.stream()
						.collect(Collectors.groupingBy(OAuth2DomainInfo::getScheme))
						.forEach((schemeType, domainInfosBySchemeType) -> {
							if (domainInfosBySchemeType.size() > 1) {
								throw new DataValidationException("Domain name and protocol must be unique within OAuth2 parameters!");
							}
						});
				});
			if (oauth2Params.getMobileInfos() != null) {
				for (OAuth2MobileInfo mobileInfo : oauth2Params.getMobileInfos()) {
					if (StringUtils.isEmpty(mobileInfo.getPkgName())) {
						throw new DataValidationException("Package should be specified!");
					}
					if (StringUtils.isEmpty(mobileInfo.getAppSecret())) {
						throw new DataValidationException("Application secret should be specified!");
					}
					if (mobileInfo.getAppSecret().length() < 16) {
						throw new DataValidationException("Application secret should be at least 16 characters!");
					}
				}
				oauth2Params.getMobileInfos().stream()
					.collect(Collectors.groupingBy(OAuth2MobileInfo::getPkgName))
					.forEach((pkgName, mobileInfos) -> {
						if (mobileInfos.size() > 1) {
							throw new DataValidationException("Mobile app package name must be unique within OAuth2 parameters!");
						}
					});
			}
			if (oauth2Params.getClientRegistrations() == null || oauth2Params.getClientRegistrations().isEmpty()) {
				throw new DataValidationException("Client registrations should be specified!");
			}
			for (OAuth2RegistrationInfo clientRegistration : oauth2Params.getClientRegistrations()) {
				if (StringUtils.isEmpty(clientRegistration.getClientId())) {
					throw new DataValidationException("Client ID should be specified!");
				}
				if (StringUtils.isEmpty(clientRegistration.getClientSecret())) {
					throw new DataValidationException("Client secret should be specified!");
				}
				if (StringUtils.isEmpty(clientRegistration.getAuthorizationUri())) {
					throw new DataValidationException("Authorization uri should be specified!");
				}
				if (StringUtils.isEmpty(clientRegistration.getAccessTokenUri())) {
					throw new DataValidationException("Token uri should be specified!");
				}
				if (CollectionUtils.isEmpty(clientRegistration.getScope())) {
					throw new DataValidationException("Scope should be specified!");
				}
				if (StringUtils.isEmpty(clientRegistration.getUserNameAttributeName())) {
					throw new DataValidationException("User name attribute name should be specified!");
				}
				if (StringUtils.isEmpty(clientRegistration.getClientAuthenticationMethod())) {
					throw new DataValidationException("Client authentication method should be specified!");
				}
				if (StringUtils.isEmpty(clientRegistration.getLoginButtonLabel())) {
					throw new DataValidationException("Login button label should be specified!");
				}
				OAuth2MapperConfig mapperConfig = clientRegistration.getMapperConfig();
				if (mapperConfig == null) {
					throw new DataValidationException("Mapper config should be specified!");
				}
				if (mapperConfig.getType() == null) {
					throw new DataValidationException("Mapper config type should be specified!");
				}
				if (mapperConfig.getType() == MapperType.BASIC) {
					OAuth2BasicMapperConfig basicConfig = mapperConfig.getBasic();
					if (basicConfig == null) {
						throw new DataValidationException("Basic config should be specified!");
					}
					if (StringUtils.isEmpty(basicConfig.getEmailAttributeKey())) {
						throw new DataValidationException("Email attribute key should be specified!");
					}
					if (basicConfig.getTenantNameStrategy() == null) {
						throw new DataValidationException("Tenant name strategy should be specified!");
					}
					if (basicConfig.getTenantNameStrategy() == TenantNameStrategyType.CUSTOM
						&& StringUtils.isEmpty(basicConfig.getTenantNamePattern())) {
						throw new DataValidationException("Tenant name pattern should be specified!");
					}
				}
				if (mapperConfig.getType() == MapperType.GITHUB) {
					OAuth2BasicMapperConfig basicConfig = mapperConfig.getBasic();
					if (basicConfig == null) {
						throw new DataValidationException("Basic config should be specified!");
					}
					if (!StringUtils.isEmpty(basicConfig.getEmailAttributeKey())) {
						throw new DataValidationException("Email attribute key cannot be configured for GITHUB mapper type!");
					}
					if (basicConfig.getTenantNameStrategy() == null) {
						throw new DataValidationException("Tenant name strategy should be specified!");
					}
					if (basicConfig.getTenantNameStrategy() == TenantNameStrategyType.CUSTOM
						&& StringUtils.isEmpty(basicConfig.getTenantNamePattern())) {
						throw new DataValidationException("Tenant name pattern should be specified!");
					}
				}
				if (mapperConfig.getType() == MapperType.CUSTOM) {
					OAuth2CustomMapperConfig customConfig = mapperConfig.getCustom();
					if (customConfig == null) {
						throw new DataValidationException("Custom config should be specified!");
					}
					if (StringUtils.isEmpty(customConfig.getUrl())) {
						throw new DataValidationException("Custom mapper URL should be specified!");
					}
				}
			}
		}
	}
}
