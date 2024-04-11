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
package org.thingsboard.domain.user.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.common.model.BaseDataWithExtra;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserCredential extends BaseDataWithExtra<Long> {
	private static final long serialVersionUID = -2108436378880529163L;

	private Long userId;
	private boolean enabled;
	private String password;
	private String activateToken;
	private String resetToken;
}
