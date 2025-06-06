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
package org.thingsboard.domain.tenant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by igor on 2/27/18.
 */

@AllArgsConstructor
public class ShortMerchantInfo {

	@Getter
	@Setter
	private Long merchantId;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private boolean isPublic;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ShortMerchantInfo that = (ShortMerchantInfo) o;

		return merchantId.equals(that.merchantId);

	}

	@Override
	public int hashCode() {
		return merchantId.hashCode();
	}
}
