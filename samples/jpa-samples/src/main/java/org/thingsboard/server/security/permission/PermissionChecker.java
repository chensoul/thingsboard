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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.server.security.SecurityUser;

public interface PermissionChecker<I extends Serializable, T extends HasTenantId> {

	default boolean hasPermission(SecurityUser user, Operation operation) {
		return false;
	}

	default boolean hasPermission(SecurityUser user, Operation operation, I entityId, T entity) {
		return false;
	}

	public class GenericPermissionChecker<I extends Serializable, T extends HasTenantId> implements PermissionChecker<I, T> {

		private final Set<Operation> allowedOperations;

		public GenericPermissionChecker(Operation... operations) {
			allowedOperations = new HashSet<Operation>(Arrays.asList(operations));
		}

		@Override
		public boolean hasPermission(SecurityUser user, Operation operation) {
			return allowedOperations.contains(Operation.ALL) || allowedOperations.contains(operation);
		}

		@Override
		public boolean hasPermission(SecurityUser user, Operation operation, I entityId, T entity) {
			return allowedOperations.contains(Operation.ALL) || allowedOperations.contains(operation);
		}
	}

	public static PermissionChecker denyAllPermissionChecker = new PermissionChecker() {
	};

	public static PermissionChecker allowAllPermissionChecker = new PermissionChecker<Serializable, HasTenantId>() {

		@Override
		public boolean hasPermission(SecurityUser user, Operation operation) {
			return true;
		}

		@Override
		public boolean hasPermission(SecurityUser user, Operation operation, Serializable entityId, HasTenantId entity) {
			return true;
		}
	};


}
