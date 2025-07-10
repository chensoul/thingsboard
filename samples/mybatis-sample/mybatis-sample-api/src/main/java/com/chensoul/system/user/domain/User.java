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
package com.chensoul.system.user.domain;

import com.chensoul.data.model.BaseDataWithExtra;
import com.chensoul.data.model.HasEmail;
import com.chensoul.data.model.HasMerchantId;
import com.chensoul.data.model.HasName;
import com.chensoul.data.model.HasTenantId;
import com.chensoul.data.validation.NoXss;
import com.chensoul.data.validation.StringLength;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.user.model.NotificationRecipient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class User extends BaseDataWithExtra<Long> implements HasTenantId, HasName, HasEmail, HasMerchantId, NotificationRecipient {
    private static final long serialVersionUID = 8250339805336035966L;
    @NoXss
    @StringLength(message = "长度不能大于{max}")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @NoXss
    private String phone;

    @NoXss
    @StringLength
    @Pattern(regexp = EMAIL_REGEXP, message = "Email address is not valid")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotNull(message = "角色不能为空")
    private Authority authority;

    private String tenantId;

    private Long merchantId;

    @JsonIgnore
    public boolean isSystemAdmin() {
        return tenantId == null || SYS_TENANT_ID.equals(tenantId);
    }

    @JsonIgnore
    public boolean isTenantAdmin() {
        return !isSystemAdmin() && merchantId == null;
    }

    @JsonIgnore
    public boolean isMerchantUser() {
        return !isSystemAdmin() && !isTenantAdmin();
    }
}
