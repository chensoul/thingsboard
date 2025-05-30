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
package com.chensoul.system.infrastructure.security.oauth2;

import com.chensoul.system.domain.oauth2.domain.OAuth2MapperConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.util.StringUtils;

@Slf4j
public class BasicMapperUtils {
    private static final String START_PLACEHOLDER_PREFIX = "%{";
    private static final String END_PLACEHOLDER_PREFIX = "}";

    public static OAuth2User getOAuth2User(String email, Map<String, Object> attributes, OAuth2MapperConfig config) {
        OAuth2User oauth2User = new OAuth2User();
        oauth2User.setEmail(email);
        oauth2User.setTenantName(getTenantName(email, attributes, config));

        String lastName = "", firstName = "";
        if (!StringUtils.isEmpty(config.getBasic().getLastNameAttributeKey())) {
            lastName = getStringAttributeByKey(attributes, config.getBasic().getLastNameAttributeKey());
        }
        if (!StringUtils.isEmpty(config.getBasic().getFirstNameAttributeKey())) {
            firstName = getStringAttributeByKey(attributes, config.getBasic().getFirstNameAttributeKey());
        }
        oauth2User.setName(StringUtils.isEmpty(lastName) ? firstName : firstName + " " + lastName);

        if (!StringUtils.isEmpty(config.getBasic().getMerchantNamePattern())) {
            StrSubstitutor sub = new StrSubstitutor(attributes, START_PLACEHOLDER_PREFIX, END_PLACEHOLDER_PREFIX);
            String merchantName = sub.replace(config.getBasic().getMerchantNamePattern());
            oauth2User.setMerchantName(merchantName);
        }
        return oauth2User;
    }

    public static String getTenantName(String email, Map<String, Object> attributes, OAuth2MapperConfig config) {
        switch (config.getBasic().getTenantNameStrategy()) {
            case EMAIL:
                return email;
            case DOMAIN:
                return email.substring(email.indexOf("@") + 1);
            case CUSTOM:
                StrSubstitutor sub = new StrSubstitutor(attributes, START_PLACEHOLDER_PREFIX, END_PLACEHOLDER_PREFIX);
                return sub.replace(config.getBasic().getTenantNamePattern());
            default:
                throw new RuntimeException("Tenant Name Strategy with type " + config.getBasic().getTenantNameStrategy() + " is not supported!");
        }
    }

    public static String getStringAttributeByKey(Map<String, Object> attributes, String key) {
        String result = null;
        try {
            result = (String) attributes.get(key);
        } catch (Exception e) {
            log.warn("Can't convert attribute to String by key " + key);
        }
        return result;
    }
}
