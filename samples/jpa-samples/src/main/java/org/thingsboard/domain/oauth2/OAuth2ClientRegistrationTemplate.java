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

import jakarta.validation.Valid;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.thingsboard.common.model.ExtraBaseData;
import org.thingsboard.common.model.HasName;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class OAuth2ClientRegistrationTemplate extends ExtraBaseData<Long> implements HasName {

    private String providerId;
    @Valid
    private OAuth2MapperConfig mapperConfig;
    private String authorizationUri;
    private String accessTokenUri;
    private List<String> scope;
    private String userInfoUri;
    private String userNameAttributeName;
    private String jwkSetUri;
    private String clientAuthenticationMethod;
    private String comment;
    private String loginButtonIcon;
    private String loginButtonLabel;
    private String helpLink;

    @Override
    public String getName() {
        return providerId;
    }
}
