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
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.data.dao.jpa.JsonConverter;
import org.thingsboard.data.dao.jpa.LongBaseEntity;
import org.thingsboard.domain.oauth2.MapperType;
import org.thingsboard.domain.oauth2.OAuth2BasicMapperConfig;
import org.thingsboard.domain.oauth2.OAuth2ClientRegistrationTemplate;
import org.thingsboard.domain.oauth2.OAuth2MapperConfig;
import org.thingsboard.domain.oauth2.internal.TenantNameStrategyType;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "oauth2_client_registration_template")
public class OAuth2ClientRegistrationTemplateEntity extends LongBaseEntity<OAuth2ClientRegistrationTemplate> {

	private String providerId;
	private String authorizationUri;
	private String tokenUri;
	private String scope;
	private String userInfoUri;
	private String userNameAttributeName;
	private String jwkSetUri;
	private String clientAuthenticationMethod;
	@Enumerated(EnumType.STRING)
	private MapperType type;
	private String emailAttributeKey;
	private String firstNameAttributeKey;
	private String lastNameAttributeKey;
	@Enumerated(EnumType.STRING)
	private TenantNameStrategyType tenantNameStrategy;
	private String tenantNamePattern;
	private String merchantNamePattern;
	private String comment;
	private String loginButtonIcon;
	private String loginButtonLabel;
	private String helpLink;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "jsonb")
	private JsonNode extra;

	@Override
	public OAuth2ClientRegistrationTemplate toData() {
		OAuth2ClientRegistrationTemplate clientRegistrationTemplate = new OAuth2ClientRegistrationTemplate();
		clientRegistrationTemplate.setId(id);
		clientRegistrationTemplate.setCreatedTime(createdTime);
		clientRegistrationTemplate.setExtra(extra);

		clientRegistrationTemplate.setProviderId(providerId);
		clientRegistrationTemplate.setMapperConfig(
			OAuth2MapperConfig.builder()
				.type(type)
				.basic(OAuth2BasicMapperConfig.builder()
					.emailAttributeKey(emailAttributeKey)
					.firstNameAttributeKey(firstNameAttributeKey)
					.lastNameAttributeKey(lastNameAttributeKey)
					.tenantNameStrategy(tenantNameStrategy)
					.tenantNamePattern(tenantNamePattern)
					.merchantNamePattern(merchantNamePattern)
					.build()
				)
				.build()
		);
		clientRegistrationTemplate.setAuthorizationUri(authorizationUri);
		clientRegistrationTemplate.setAccessTokenUri(tokenUri);
		clientRegistrationTemplate.setScope(Arrays.asList(scope.split(",")));
		clientRegistrationTemplate.setUserInfoUri(userInfoUri);
		clientRegistrationTemplate.setUserNameAttributeName(userNameAttributeName);
		clientRegistrationTemplate.setJwkSetUri(jwkSetUri);
		clientRegistrationTemplate.setClientAuthenticationMethod(clientAuthenticationMethod);
		clientRegistrationTemplate.setComment(comment);
		clientRegistrationTemplate.setLoginButtonIcon(loginButtonIcon);
		clientRegistrationTemplate.setLoginButtonLabel(loginButtonLabel);
		clientRegistrationTemplate.setHelpLink(helpLink);
		return clientRegistrationTemplate;
	}
}
