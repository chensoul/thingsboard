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
package com.chensoul.system.domain.oauth2.service;

import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.oauth2.domain.OAuth2ClientRegistrationTemplate;
import com.chensoul.validation.DataValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ClientRegistrationTemplateValidator extends DataValidator<OAuth2ClientRegistrationTemplate> {

    @Override
    protected void validateCreate(OAuth2ClientRegistrationTemplate clientRegistrationTemplate) {
    }

    @Override
    protected OAuth2ClientRegistrationTemplate validateUpdate(OAuth2ClientRegistrationTemplate clientRegistrationTemplate) {
        return clientRegistrationTemplate;
    }

    @Override
    protected void validateDataImpl(OAuth2ClientRegistrationTemplate clientRegistrationTemplate) {
        if (StringUtils.isEmpty(clientRegistrationTemplate.getProviderId())) {
            throw new BusinessException("Provider ID should be specified!");
        }
        if (clientRegistrationTemplate.getMapperConfig() == null) {
            throw new BusinessException("Mapper config should be specified!");
        }
        if (clientRegistrationTemplate.getMapperConfig().getType() == null) {
            throw new BusinessException("Mapper type should be specified!");
        }
        if (clientRegistrationTemplate.getMapperConfig().getBasic() == null) {
            throw new BusinessException("Basic mapper config should be specified!");
        }
    }
}
