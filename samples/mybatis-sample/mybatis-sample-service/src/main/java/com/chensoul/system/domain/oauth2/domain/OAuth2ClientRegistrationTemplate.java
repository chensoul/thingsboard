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
package com.chensoul.system.domain.oauth2.domain;

import com.chensoul.data.model.BaseDataWithExtra;
import com.chensoul.data.model.HasName;
import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class OAuth2ClientRegistrationTemplate extends BaseDataWithExtra<Long> implements HasName {

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
