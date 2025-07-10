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
package org.thingsboard.domain.merchant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.ContactBased;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;
import org.thingsboard.domain.tenant.ShortMerchantInfo;

@Data
@EqualsAndHashCode(callSuper = true)
public class Merchant extends ContactBased<Long> implements HasTenantId, HasName {

	private static final long serialVersionUID = -1599722990298929275L;

	@NoXss
	@Length
	@NotBlank(message = "Name should be specified")
	private String name;

	private String tenantId;

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
