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

import com.chensoul.system.domain.oauth2.domain.OAuth2MapperConfig;
import com.chensoul.system.domain.oauth2.domain.OAuth2Registration;
import com.chensoul.system.infrastructure.security.oauth2.BasicMapperUtils;
import com.chensoul.system.infrastructure.security.oauth2.OAuth2Configuration;
import com.chensoul.system.infrastructure.security.oauth2.OAuth2User;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service(value = "githubOAuth2ClientMapper")
@Slf4j
public class GithubOAuth2ClientMapper extends AbstractOAuth2ClientMapper implements OAuth2ClientMapper {
    private static final String EMAIL_URL_KEY = "emailUrl";
    private static final String AUTHORIZATION = "Authorization";
    private RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();

    @Autowired
    private OAuth2Configuration oAuth2Configuration;

    @Override
    protected OAuth2User getOAuth2User(HttpServletRequest request, OAuth2AuthenticationToken token, String providerAccessToken, OAuth2Registration registration) {
        OAuth2MapperConfig config = registration.getMapperConfig();
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        String email = getEmail(attributes, config, providerAccessToken);
        return BasicMapperUtils.getOAuth2User(email, attributes, config);
    }

    protected String getEmail(Map<String, Object> attributes, OAuth2MapperConfig config, String providerAccessToken) {
        String email = BasicMapperUtils.getStringAttributeByKey(attributes, config.getBasic().getEmailAttributeKey());
        if (StringUtils.isBlank(email)) {
            Map<String, String> githubMapperConfig = oAuth2Configuration.getGithubMapper();
            email = getEmail(githubMapperConfig.get(EMAIL_URL_KEY), providerAccessToken);
        }
        return email;
    }

    private synchronized String getEmail(String emailUrl, String oauth2Token) {
        restTemplateBuilder = restTemplateBuilder.defaultHeader(AUTHORIZATION, "token " + oauth2Token);

        RestTemplate restTemplate = restTemplateBuilder.build();
        GithubEmailsResponse githubEmailsResponse;
        try {
            githubEmailsResponse = restTemplate.getForEntity(emailUrl, GithubEmailsResponse.class).getBody();
            if (githubEmailsResponse == null) {
                throw new RuntimeException("Empty Github response!");
            }
        } catch (Exception e) {
            log.error("There was an error during connection to Github API", e);
            throw new RuntimeException("Unable to login. Please contact your Administrator!");
        }
        Optional<String> emailOpt = githubEmailsResponse.stream()
            .filter(GithubEmailResponse::isPrimary)
            .map(GithubEmailResponse::getEmail)
            .findAny();
        if (emailOpt.isPresent()) {
            return emailOpt.get();
        } else {
            log.error("Could not find primary email from {}.", githubEmailsResponse);
            throw new RuntimeException("Unable to login. Please contact your Administrator!");
        }
    }

    private static class GithubEmailsResponse extends ArrayList<GithubEmailResponse> {
    }

    @Data
    @ToString
    private static class GithubEmailResponse {
        private String email;
        private boolean verified;
        private boolean primary;
        private String visibility;
    }
}
