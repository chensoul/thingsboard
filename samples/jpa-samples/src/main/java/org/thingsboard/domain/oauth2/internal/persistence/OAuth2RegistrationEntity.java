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
package org.thingsboard.domain.oauth2.internal.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.data.dao.jpa.JsonConverter;
import org.thingsboard.data.dao.jpa.StringBaseEntity;
import org.thingsboard.domain.oauth2.MapperType;
import org.thingsboard.domain.oauth2.OAuth2BasicMapperConfig;
import org.thingsboard.domain.oauth2.OAuth2CustomMapperConfig;
import org.thingsboard.domain.oauth2.OAuth2MapperConfig;
import org.thingsboard.domain.oauth2.OAuth2Registration;
import org.thingsboard.domain.oauth2.PlatformType;
import org.thingsboard.domain.oauth2.internal.TenantNameStrategyType;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "oauth2_registration")
public class OAuth2RegistrationEntity extends StringBaseEntity<OAuth2Registration> {
	@Column(name = "oauth2_param_id", nullable = false)
	private Long oauth2ParamId;

	private String clientId;
	private String clientSecret;
	private String authorizationUri;
	private String tokenUri;
	private String scope;
	private String platforms;
	private String userInfoUri;
	private String userNameAttributeName;
	private String jwkSetUri;
	private String clientAuthenticationMethod;
	private String loginButtonLabel;
	private String loginButtonIcon;
	private Boolean allowUserCreation;
	private Boolean activateUser;
	@Enumerated(EnumType.STRING)
	private MapperType type;
	private String emailAttributeKey;
	private String firstNameAttributeKey;
	private String lastNameAttributeKey;
	@Enumerated(EnumType.STRING)
	private TenantNameStrategyType tenantNameStrategy;
	private String tenantNamePattern;
	private String merchantNamePattern;
	private String customUrl;
	private String customUsername;
	private String customPassword;
	private Boolean customSendToken;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "jsonb")
	private JsonNode extra;

	@Override
	public OAuth2Registration toData() {
		OAuth2Registration registration = new OAuth2Registration();
		registration.setId(id);
		registration.setCreatedTime(createdTime);
		registration.setOauth2ParamId(oauth2ParamId);
		registration.setExtra(extra);
		registration.setMapperConfig(
			OAuth2MapperConfig.builder()
				.allowUserCreation(allowUserCreation)
				.activateUser(activateUser)
				.type(type)
				.basic(
					(type == MapperType.BASIC || type == MapperType.GITHUB || type == MapperType.APPLE) ?
						OAuth2BasicMapperConfig.builder()
							.emailAttributeKey(emailAttributeKey)
							.firstNameAttributeKey(firstNameAttributeKey)
							.lastNameAttributeKey(lastNameAttributeKey)
							.tenantNameStrategy(tenantNameStrategy)
							.tenantNamePattern(tenantNamePattern)
							.merchantNamePattern(merchantNamePattern)
							.build()
						: null
				)
				.custom(
					type == MapperType.CUSTOM ?
						OAuth2CustomMapperConfig.builder()
							.url(customUrl)
							.username(customUsername)
							.password(customPassword)
							.sendToken(customSendToken)
							.build()
						: null
				)
				.build()
		);
		registration.setClientId(clientId);
		registration.setClientSecret(clientSecret);
		registration.setAuthorizationUri(authorizationUri);
		registration.setAccessTokenUri(tokenUri);
		registration.setScope(Arrays.asList(scope.split(",")));
		registration.setPlatforms(StringUtils.isNotEmpty(platforms) ? Arrays.stream(platforms.split(","))
			.map(str -> PlatformType.valueOf(str)).collect(Collectors.toList()) : Collections.emptyList());
		registration.setUserInfoUri(userInfoUri);
		registration.setUserNameAttributeName(userNameAttributeName);
		registration.setJwkSetUri(jwkSetUri);
		registration.setClientAuthenticationMethod(clientAuthenticationMethod);
		registration.setLoginButtonLabel(loginButtonLabel);
		registration.setLoginButtonIcon(loginButtonIcon);
		return registration;
	}
}
