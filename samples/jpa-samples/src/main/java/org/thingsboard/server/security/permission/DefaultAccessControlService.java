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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.domain.user.Authority;
import org.thingsboard.server.security.SecurityUser;

@Service
@Slf4j
public class DefaultAccessControlService implements AccessControlService {

	private static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
	private static final String YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION = "You don't have permission to perform this operation!";

	private final Map<Authority, Permission> authorityPermissions = new HashMap<>();

	public DefaultAccessControlService(
		@Qualifier("sysAdminPermission") Permission sysAdminPermission,
		@Qualifier("tenantAdminPermission") Permission tenantAdminPermission,
		@Qualifier("merchantUserPermission") Permission merchantUserPermission) {
		authorityPermissions.put(Authority.SYS_ADMIN, sysAdminPermission);
		authorityPermissions.put(Authority.TENANT_ADMIN, tenantAdminPermission);
		authorityPermissions.put(Authority.MERCHANT_USER, merchantUserPermission);
	}

	@Override
	public void checkPermission(SecurityUser user, Resource resource, Operation operation) throws ThingsboardException {
		PermissionChecker permissionChecker = getPermissionChecker(user.getAuthority(), resource);
		if (!permissionChecker.hasPermission(user, operation)) {
			permissionDenied();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends Serializable, T extends HasTenantId> void checkPermission(SecurityUser user, Resource resource,
																				Operation operation, I entityId, T entity) throws ThingsboardException {
		PermissionChecker permissionChecker = getPermissionChecker(user.getAuthority(), resource);
		if (!permissionChecker.hasPermission(user, operation, entityId, entity)) {
			permissionDenied();
		}
	}

	private PermissionChecker getPermissionChecker(Authority authority, Resource resource) throws ThingsboardException {
		Permission permission = authorityPermissions.get(authority);
		if (permission == null) {
			permissionDenied();
		}
		Optional<PermissionChecker> permissionChecker = permission.getPermissionChecker(resource);
		if (!permissionChecker.isPresent()) {
			permissionDenied();
		}
		return permissionChecker.get();
	}

	private void permissionDenied() throws ThingsboardException {
		throw new ThingsboardException(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION, ThingsboardErrorCode.PERMISSION_DENIED);
	}

}
