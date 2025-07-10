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
package org.thingsboard.server.security.permission;

import java.io.Serializable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.domain.user.Authority;
import org.thingsboard.domain.user.User;
import org.thingsboard.server.security.SecurityUser;

@Component(value = "sysAdminPermission")
public class SysAdminPermission extends AbstractPermission {

	public SysAdminPermission() {
		super();
		put(Resource.SYSTEM_SETTING, PermissionChecker.allowAllPermissionChecker);
		put(Resource.TENANT, PermissionChecker.allowAllPermissionChecker);
		put(Resource.USER, userPermissionChecker);
		put(Resource.OAUTH2_CONFIGURATION_INFO, PermissionChecker.allowAllPermissionChecker);
		put(Resource.OAUTH2_CONFIGURATION_TEMPLATE, PermissionChecker.allowAllPermissionChecker);
		put(Resource.TENANT_PROFILE, PermissionChecker.allowAllPermissionChecker);
		put(Resource.NOTIFICATION, systemEntityPermissionChecker);
	}

	private static final PermissionChecker systemEntityPermissionChecker = new PermissionChecker() {

		@Override
		public boolean hasPermission(SecurityUser user, Operation operation, Serializable entityId, HasTenantId entity) {

			if (entity.getTenantId() != null) {
				return false;
			}
			return true;
		}
	};

	private static final PermissionChecker userPermissionChecker = new PermissionChecker<Long, User>() {

		@Override
		public boolean hasPermission(SecurityUser user, Operation operation, Long userId, User userEntity) {
			if (Authority.MERCHANT_USER.equals(userEntity.getAuthority())) {
				return false;
			}
			return true;
		}

	};

}
