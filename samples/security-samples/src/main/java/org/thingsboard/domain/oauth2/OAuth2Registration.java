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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.thingsboard.common.model.BaseDataWithExtra;
import org.thingsboard.common.model.HasName;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(exclude = {"clientSecret"})
@NoArgsConstructor
public class OAuth2Registration extends BaseDataWithExtra<Long> implements HasName {

	private Long oauth2ParamId;
	private OAuth2MapperConfig mapperConfig;
	private String clientId;
	private String clientSecret;
	private String authorizationUri;
	private String accessTokenUri;
	private List<String> scope;
	private String userInfoUri;
	private String userNameAttributeName;
	private String jwkSetUri;
	private String clientAuthenticationMethod;
	private String loginButtonLabel;
	private String loginButtonIcon;
	private List<PlatformType> platforms;

	@Override
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getName() {
		return loginButtonLabel;
	}
}
