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
package com.chensoul.system.infrastructure.security.oauth2.mapper;

import com.chensoul.json.JacksonUtils;
import com.chensoul.system.domain.oauth2.domain.OAuth2CustomMapperConfig;
import com.chensoul.system.domain.oauth2.domain.OAuth2MapperConfig;
import com.chensoul.system.domain.oauth2.domain.OAuth2Registration;
import com.chensoul.system.infrastructure.security.oauth2.OAuth2User;
import com.fasterxml.jackson.core.JsonProcessingException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service(value = "customOAuth2ClientMapper")
@Slf4j
public class CustomOAuth2ClientMapper extends AbstractOAuth2ClientMapper implements OAuth2ClientMapper {
    private static final String PROVIDER_ACCESS_TOKEN = "provider-access-token";

    private RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();

    @Override
    protected OAuth2User getOAuth2User(HttpServletRequest request, OAuth2AuthenticationToken token, String providerAccessToken, OAuth2Registration registration) {
        OAuth2MapperConfig config = registration.getMapperConfig();
        return getOAuth2User(token, config.getCustom(), providerAccessToken);
    }

    private synchronized OAuth2User getOAuth2User(OAuth2AuthenticationToken token, OAuth2CustomMapperConfig custom, String providerAccessToken) {
        if (!StringUtils.isEmpty(custom.getUsername()) && !StringUtils.isEmpty(custom.getPassword())) {
            restTemplateBuilder = restTemplateBuilder.basicAuthentication(custom.getUsername(), custom.getPassword());
        }
        if (custom.isSendToken() && !StringUtils.isEmpty(providerAccessToken)) {
            restTemplateBuilder = restTemplateBuilder.defaultHeader(PROVIDER_ACCESS_TOKEN, providerAccessToken);
        }

        RestTemplate restTemplate = restTemplateBuilder.build();
        String request;
        try {
            request = JacksonUtils.OBJECT_MAPPER.writeValueAsString(token.getPrincipal());
        } catch (JsonProcessingException e) {
            log.error("Can't convert principal to JSON string", e);
            throw new RuntimeException("Can't convert principal to JSON string", e);
        }
        try {
            return restTemplate.postForEntity(custom.getUrl(), request, OAuth2User.class).getBody();
        } catch (Exception e) {
            log.error("There was an error during connection to custom mapper endpoint", e);
            throw new RuntimeException("Unable to login. Please contact your Administrator!");
        }
    }
}
