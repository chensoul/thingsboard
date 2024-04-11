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
package org.thingsboard.domain.oauth2;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.entity.LongBaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oauth2_client_registration_template", autoResultMap = true)
public class OAuth2ClientRegistrationTemplateEntity extends LongBaseEntity<OAuth2ClientRegistrationTemplate> {

	private String providerId;
	private String authorizationUri;
	private String tokenUri;
	private String scope;
	private String userInfoUri;
	private String userNameAttributeName;
	private String jwkSetUri;
	private String clientAuthenticationMethod;
	private MapperType type;
	private String emailAttributeKey;
	private String firstNameAttributeKey;
	private String lastNameAttributeKey;
	private TenantNameStrategyType tenantNameStrategy;
	private String tenantNamePattern;
	private String customerNamePattern;
	private String comment;
	private String loginButtonIcon;
	private String loginButtonLabel;
	private String helpLink;

	@TableField(typeHandler = JacksonTypeHandler.class)
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
					.customerNamePattern(customerNamePattern)
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
