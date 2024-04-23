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
package org.thingsboard.domain.oauth2.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.data.dao.jpa.LongBaseEntity;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.oauth2.model.OAuth2Mobile;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "oauth2_mobile")
public class OAuth2MobileEntity extends LongBaseEntity<OAuth2Mobile> {

	@Column(name = "oauth2_param_id", nullable = false)
	private Long oauth2ParamId;

	private String pkgName;

	private String appSecret;

	@Override
	public OAuth2Mobile toData() {
		return JacksonUtil.convertValue(this, OAuth2Mobile.class);
	}
}
