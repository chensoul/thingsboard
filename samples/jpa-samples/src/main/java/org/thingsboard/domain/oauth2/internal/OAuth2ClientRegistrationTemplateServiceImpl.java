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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thingsboard.domain.oauth2.OAuth2ClientRegistrationTemplate;
import org.thingsboard.domain.oauth2.OAuth2ClientRegistrationTemplateService;
import org.thingsboard.domain.oauth2.internal.persistence.OAuth2ClientRegistrationTemplateDao;

@Slf4j
@Service
@AllArgsConstructor
public class OAuth2ClientRegistrationTemplateServiceImpl implements OAuth2ClientRegistrationTemplateService {
	private final OAuth2ClientRegistrationTemplateDao clientRegistrationTemplateDao;
	private final OAuth2ClientRegistrationTemplateValidator OAuth2ClientRegistrationTemplateValidator;

	@Override
	public OAuth2ClientRegistrationTemplate saveClientRegistrationTemplate(OAuth2ClientRegistrationTemplate clientRegistrationTemplate) {
		OAuth2ClientRegistrationTemplateValidator.validate(clientRegistrationTemplate);
		return clientRegistrationTemplateDao.save(clientRegistrationTemplate);
	}

	@Override
	public OAuth2ClientRegistrationTemplate findClientRegistrationTemplateByProviderId(String providerId) {
		return clientRegistrationTemplateDao.findByProviderId(providerId);
	}

	@Override
	public OAuth2ClientRegistrationTemplate findClientRegistrationTemplateById(Long templateId) {
		return clientRegistrationTemplateDao.findById(templateId);
	}

	@Override
	public List<OAuth2ClientRegistrationTemplate> findAllClientRegistrationTemplates() {
		return clientRegistrationTemplateDao.findAll();
	}

	@Override
	public void deleteClientRegistrationTemplateById(Long templateId) {
		clientRegistrationTemplateDao.removeById(templateId);
	}
}
