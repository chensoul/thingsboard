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

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.thingsboard.domain.oauth2.OAuth2MapperConfig;
import org.thingsboard.domain.oauth2.OAuth2Registration;
import org.thingsboard.server.security.oauth2.BasicMapperUtils;
import org.thingsboard.server.security.oauth2.OAuth2User;

@Service(value = "basicOAuth2ClientMapper")
@Slf4j
public class BasicOAuth2ClientMapper extends AbstractOAuth2ClientMapper implements OAuth2ClientMapper {

	@Override
	protected OAuth2User getOAuth2User(HttpServletRequest request, OAuth2AuthenticationToken token, String providerAccessToken, OAuth2Registration registration) {
		OAuth2MapperConfig config = registration.getMapperConfig();
		Map<String, Object> attributes = token.getPrincipal().getAttributes();

		String email = BasicMapperUtils.getStringAttributeByKey(attributes, config.getBasic().getEmailAttributeKey());
		OAuth2User oauth2User = BasicMapperUtils.getOAuth2User(email, attributes, config);
		return oauth2User;
	}
}
