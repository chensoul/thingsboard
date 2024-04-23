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
package org.thingsboard.domain.iot.asset;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.BaseDataWithExtra;
import org.thingsboard.common.model.HasLabel;
import org.thingsboard.common.model.HasMerchantId;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

@Data
@EqualsAndHashCode(callSuper = true)
public class Asset extends BaseDataWithExtra<Long> implements HasLabel, HasName, HasTenantId, HasMerchantId {
	private static final long serialVersionUID = 2807343040519543363L;

	private String tenantId;

	private Long merchantId;

	@NoXss
	@Length
	private String name;

	@NoXss
	@Length
	private String type;

	@NoXss
	@Length
	private String label;

	private Long assetProfileId;
}
