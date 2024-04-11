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
package org.thingsboard.domain.tenant.persistence;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.entity.StringBaseEntity;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.tenant.model.Tenant;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tenant", autoResultMap = true)
public final class TenantEntity extends StringBaseEntity<Tenant> {
	private String name;

	private String country;

	private String state;

	private String city;

	private String address;

	private String address2;

	private String zip;

	private String phone;

	private String email;

	@TableField(typeHandler = JacksonTypeHandler.class)
	private JsonNode extra;

	private Long tenantProfileId;

	@Override
	public Tenant toData() {
		return JacksonUtil.convertValue(this, Tenant.class);
	}
}
