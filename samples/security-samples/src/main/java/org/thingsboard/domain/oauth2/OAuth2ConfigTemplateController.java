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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.exception.ThingsboardException;

@RestController
@RequestMapping("/api/oauth2/config/template")
@Slf4j
@RequiredArgsConstructor
public class OAuth2ConfigTemplateController {
	private static final String CLIENT_REGISTRATION_TEMPLATE_ID = "clientRegistrationTemplateId";

	private final OAuth2ConfigTemplateService oAuth2ConfigTemplateService;

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public OAuth2ClientRegistrationTemplate saveClientRegistrationTemplate(@RequestBody OAuth2ClientRegistrationTemplate clientRegistrationTemplate) {
		return oAuth2ConfigTemplateService.saveClientRegistrationTemplate(clientRegistrationTemplate);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/{clientRegistrationTemplateId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteClientRegistrationTemplate(@PathVariable(CLIENT_REGISTRATION_TEMPLATE_ID) Long clientRegistrationTemplateId) {
		oAuth2ConfigTemplateService.deleteClientRegistrationTemplateById(clientRegistrationTemplateId);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public List<OAuth2ClientRegistrationTemplate> getClientRegistrationTemplates() {
		return oAuth2ConfigTemplateService.findAllClientRegistrationTemplates();
	}
}
