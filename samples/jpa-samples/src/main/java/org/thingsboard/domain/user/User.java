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
package org.thingsboard.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.thingsboard.common.model.BaseDataWithExtra;
import org.thingsboard.common.model.HasEmail;
import org.thingsboard.common.model.HasMerchantId;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;
import org.thingsboard.domain.notification.NotificationRecipient;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class User extends BaseDataWithExtra<Long> implements HasTenantId, HasName, HasEmail, HasMerchantId, NotificationRecipient {
	private static final long serialVersionUID = 8250339805336035966L;

	@NoXss
	@Length
	@NotBlank(message = "Name should be specified.")
	private String name;

	@NoXss
	private String phone;

	@NoXss
	@Length
	@Pattern(regexp = EMAIL_REGEXP, message = "Email address is not valid")
	@NotBlank(message = "Email should be specified")
	private String email;

	@NotNull(message = "Authority should be specified.")
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
