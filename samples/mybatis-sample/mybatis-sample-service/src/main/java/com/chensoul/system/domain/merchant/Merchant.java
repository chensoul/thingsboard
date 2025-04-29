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
package com.chensoul.system.domain.merchant;

import com.chensoul.data.model.ContactBased;
import com.chensoul.data.model.HasName;
import com.chensoul.data.model.HasTenantId;
import com.chensoul.data.validation.NoXss;
import com.chensoul.data.validation.StringLength;
import com.chensoul.system.domain.tenant.domain.ShortMerchantInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Merchant extends ContactBased<Long> implements HasTenantId, HasName {

    private static final long serialVersionUID = -1599722990298929275L;

    @NoXss
    @StringLength
    @NotBlank(message = "Name should be specified")
    private String name;

    private String tenantId;

    private JsonNode extra;

    @JsonIgnore
    public boolean isPublic() {
        if (getExtra() != null && getExtra().has("isPublic")) {
            return getExtra().get("isPublic").asBoolean();
        }

        return false;
    }

    @JsonIgnore
    public ShortMerchantInfo toShortCustomerInfo() {
        return new ShortMerchantInfo(id, name, isPublic());
    }
}
