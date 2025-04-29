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
package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.domain.oauth2.domain.MapperType;
import com.chensoul.system.domain.oauth2.domain.OAuth2BasicMapperConfig;
import com.chensoul.system.domain.oauth2.domain.OAuth2ClientRegistrationTemplate;
import com.chensoul.system.domain.oauth2.domain.OAuth2MapperConfig;
import com.chensoul.system.domain.oauth2.domain.TenantNameStrategyType;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private String merchantNamePattern;
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
        clientRegistrationTemplate.setCreateTime(createTime);
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
