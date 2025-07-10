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
package org.thingsboard.domain.user.internal.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLJsonPGObjectJsonbType;
import org.thingsboard.data.dao.jpa.JsonConverter;
import org.thingsboard.data.dao.jpa.LongBaseEntity;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.user.UserCredential;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_credential")
public final class UserCredentialEntity extends LongBaseEntity<UserCredential> {

	private Long userId;

	private boolean enabled;

	private String password;

	private String activateToken;

	private String resetToken;

	@Convert(converter = JsonConverter.class)
	@JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
	@Column(name = "extra", columnDefinition = "jsonb")
	private JsonNode extra;

	@Override
	public UserCredential toData() {
		return JacksonUtil.convertValue(this, UserCredential.class);
	}
}
