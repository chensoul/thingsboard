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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OAuth2ConfigTemplateServiceImpl implements OAuth2ConfigTemplateService {
	private final OAuth2ClientRegistrationTemplateDao clientRegistrationTemplateDao;
	private final ClientRegistrationTemplateValidator clientRegistrationTemplateValidator;

	@Override
	public OAuth2ClientRegistrationTemplate saveClientRegistrationTemplate(OAuth2ClientRegistrationTemplate clientRegistrationTemplate) {
		log.trace("Executing saveClientRegistrationTemplate [{}]", clientRegistrationTemplate);
		clientRegistrationTemplateValidator.validate(clientRegistrationTemplate);
		return clientRegistrationTemplateDao.save(clientRegistrationTemplate);
	}

	@Override
	public OAuth2ClientRegistrationTemplate findClientRegistrationTemplateByProviderId(String providerId) {
		log.trace("Executing findClientRegistrationTemplateByProviderId [{}]", providerId);
		return clientRegistrationTemplateDao.findByProviderId(providerId);
	}

	@Override
	public OAuth2ClientRegistrationTemplate findClientRegistrationTemplateById(Long templateId) {
		log.trace("Executing findClientRegistrationTemplateById [{}]", templateId);
		return clientRegistrationTemplateDao.findById(templateId);
	}

	@Override
	public List<OAuth2ClientRegistrationTemplate> findAllClientRegistrationTemplates() {
		log.trace("Executing findAllClientRegistrationTemplates");
		return clientRegistrationTemplateDao.findAll();
	}

	@Override
	public void deleteClientRegistrationTemplateById(Long templateId) {
		log.trace("Executing deleteClientRegistrationTemplateById [{}]", templateId);
		clientRegistrationTemplateDao.removeById(templateId);
	}
}
