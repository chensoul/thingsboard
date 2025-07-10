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
package org.thingsboard.server.security.oauth2.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.oauth2.OAuth2MapperConfig;
import org.thingsboard.domain.oauth2.OAuth2Registration;
import org.thingsboard.server.security.oauth2.BasicMapperUtils;
import org.thingsboard.server.security.oauth2.OAuth2User;

@Service(value = "appleOAuth2ClientMapper")
@Slf4j
public class AppleOAuth2ClientMapper extends AbstractOAuth2ClientMapper implements OAuth2ClientMapper {

	private static final String USER = "user";
	private static final String NAME = "name";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String EMAIL = "email";

	@Override
	protected OAuth2User getOAuth2User(HttpServletRequest request, OAuth2AuthenticationToken token, String providerAccessToken, OAuth2Registration registration) {
		OAuth2MapperConfig config = registration.getMapperConfig();
		Map<String, Object> attributes = token.getPrincipal().getAttributes();

		Map<String, Object> updatedAttributesFromRequest = updateAttributesFromRequestParams(request, attributes);
		String email = BasicMapperUtils.getStringAttributeByKey(updatedAttributesFromRequest, config.getBasic().getEmailAttributeKey());
		OAuth2User oauth2User = BasicMapperUtils.getOAuth2User(email, updatedAttributesFromRequest, config);
		return oauth2User;
	}

	private static Map<String, Object> updateAttributesFromRequestParams(HttpServletRequest request, Map<String, Object> attributes) {
		Map<String, Object> updated = attributes;
		MultiValueMap<String, String> params = toMultiMap(request.getParameterMap());
		String userValue = params.getFirst(USER);
		if (StringUtils.hasText(userValue)) {
			JsonNode user = JacksonUtil.readTree(userValue);
			if (user != null) {
				updated = new HashMap<>(attributes);
				if (user.has(NAME)) {
					JsonNode name = user.get(NAME);
					if (name.isObject()) {
						JsonNode firstName = name.get(FIRST_NAME);
						if (firstName != null && firstName.isTextual()) {
							updated.put(FIRST_NAME, firstName.asText());
						}
						JsonNode lastName = name.get(LAST_NAME);
						if (lastName != null && lastName.isTextual()) {
							updated.put(LAST_NAME, lastName.asText());
						}
					}
				}
				if (user.has(EMAIL)) {
					JsonNode email = user.get(EMAIL);
					if (email != null && email.isTextual()) {
						updated.put(EMAIL, email.asText());
					}
				}
			}
		}
		return updated;
	}

	private static MultiValueMap<String, String> toMultiMap(Map<String, String[]> map) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(map.size());
		map.forEach((key, values) -> {
			if (values.length > 0) {
				for (String value : values) {
					params.add(key, value);
				}
			}
		});
		return params;
	}
}
