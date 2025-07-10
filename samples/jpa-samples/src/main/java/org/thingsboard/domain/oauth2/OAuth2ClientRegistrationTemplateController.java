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
package org.thingsboard.domain.oauth2;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import static org.thingsboard.common.ControllerConstants.CLIENT_REGISTRATION_TEMPLATE_ID;

@RestController
@RequestMapping("/api/oauth2/config/template")
@Slf4j
@RequiredArgsConstructor
public class OAuth2ClientRegistrationTemplateController {
	private final OAuth2ClientRegistrationTemplateService oAuth2ClientRegistrationTemplateService;

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@PostMapping
	public OAuth2ClientRegistrationTemplate saveClientRegistrationTemplate(@RequestBody OAuth2ClientRegistrationTemplate clientRegistrationTemplate) {
		return oAuth2ClientRegistrationTemplateService.saveClientRegistrationTemplate(clientRegistrationTemplate);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@DeleteMapping("/{clientRegistrationTemplateId}")
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteClientRegistrationTemplate(@PathVariable(CLIENT_REGISTRATION_TEMPLATE_ID) Long clientRegistrationTemplateId) {
		oAuth2ClientRegistrationTemplateService.deleteClientRegistrationTemplateById(clientRegistrationTemplateId);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@GetMapping
	public List<OAuth2ClientRegistrationTemplate> getClientRegistrationTemplates() {
		return oAuth2ClientRegistrationTemplateService.findAllClientRegistrationTemplates();
	}
}
