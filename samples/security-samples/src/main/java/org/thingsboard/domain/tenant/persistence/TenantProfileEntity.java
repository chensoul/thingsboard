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
import org.thingsboard.common.entity.LongBaseEntity;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.tenant.model.TenantProfile;

@Data
@TableName(value = "tenant_profile", autoResultMap = true)
public class TenantProfileEntity extends LongBaseEntity<TenantProfile> {

	private static final long serialVersionUID = 2628320657987010348L;

	private String name;

	private String description;

	private String tenantId;

	@TableField(value = "is_default")
	private Boolean isDefault;

	@TableField(typeHandler = JacksonTypeHandler.class)
	private JsonNode extra;

	@Override
	public TenantProfile toData() {
		return JacksonUtil.convertValue(this, TenantProfile.class);
	}
}
