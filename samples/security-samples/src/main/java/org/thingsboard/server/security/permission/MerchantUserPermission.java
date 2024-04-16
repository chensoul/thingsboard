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
package org.thingsboard.server.security.permission;

import java.io.Serializable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.server.security.SecurityUser;

@Component(value = "merchantUserPermission")
public class MerchantUserPermission extends AbstractPermission {

	public MerchantUserPermission() {
		super();
		put(Resource.MERCHANT, customerPermissionChecker);
		put(Resource.USER, userPermissionChecker);
	}

	private static final PermissionChecker customerPermissionChecker =
		new PermissionChecker.GenericPermissionChecker(Operation.READ) {

			@Override
			@SuppressWarnings("unchecked")
			public boolean hasPermission(SecurityUser user, Operation operation, Serializable entityId, HasTenantId entity) {
				if (!super.hasPermission(user, operation, entityId, entity)) {
					return false;
				}
				return user.getMerchantId().equals(entityId);
			}

		};

	private static final PermissionChecker userPermissionChecker = new PermissionChecker<Long, User>() {

		@Override
		public boolean hasPermission(SecurityUser user, Operation operation, Long userId, User userEntity) {
			if (!Authority.MERCHANT_USER.equals(userEntity.getAuthority())) {
				return false;
			}

			if (!user.getMerchantId().equals(userEntity.getMerchantId())) {
				return false;
			}

			if (Operation.READ.equals(operation)) {
				return true;
			}

			return user.getId().equals(userId);
		}
	};
}
