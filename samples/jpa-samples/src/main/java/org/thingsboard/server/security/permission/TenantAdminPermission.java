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

@Component(value = "tenantAdminPermission")
public class TenantAdminPermission extends AbstractPermission {

	public TenantAdminPermission() {
		super();
		put(Resource.SYSTEM_SETTING, PermissionChecker.allowAllPermissionChecker);
		put(Resource.MERCHANT, tenantEntityPermissionChecker);
		put(Resource.TENANT, tenantPermissionChecker);
		put(Resource.USER, userPermissionChecker);
		put(Resource.VERSION_CONTROL, PermissionChecker.allowAllPermissionChecker);
		put(Resource.NOTIFICATION, tenantEntityPermissionChecker);
	}

	public static final PermissionChecker tenantEntityPermissionChecker = new PermissionChecker() {
		@Override
		public boolean hasPermission(SecurityUser user, Operation operation, Serializable entityId, HasTenantId entity) {
			if (!user.getTenantId().equals(entity.getTenantId())) {
				return false;
			}
			return true;
		}
	};

	private static final PermissionChecker tenantPermissionChecker =
		new PermissionChecker.GenericPermissionChecker(Operation.READ) {
			@Override
			@SuppressWarnings("unchecked")
			public boolean hasPermission(SecurityUser user, Operation operation, Serializable entityId, HasTenantId entity) {
				if (!super.hasPermission(user, operation, entityId, entity)) {
					return false;
				}
				if (!user.getTenantId().equals(entityId)) {
					return false;
				}
				return true;
			}

		};

	private static final PermissionChecker userPermissionChecker = new PermissionChecker<Long, User>() {

		@Override
		public boolean hasPermission(SecurityUser user, Operation operation, Long userId, User userEntity) {
			if (Authority.SYS_ADMIN.equals(userEntity.getAuthority())) {
				return false;
			}
			if (!user.getTenantId().equals(userEntity.getTenantId())) {
				return false;
			}
			return true;
		}

	};
}
