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
package com.chensoul.system.domain.tenant.mybatis;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.domain.tenant.domain.TenantProfile;
import lombok.Data;

@Data
@TableName(value = "tenant_profile")
public class TenantProfileEntity extends LongBaseEntity<TenantProfile> {

    private static final long serialVersionUID = 2628320657987010348L;

    private String name;

    private String description;

    private boolean defaulted;

    @Override
    public TenantProfile toData() {
        return JacksonUtils.convertValue(this, TenantProfile.class);
    }
}
