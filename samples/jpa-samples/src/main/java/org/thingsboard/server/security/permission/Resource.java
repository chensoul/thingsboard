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

import java.util.Collections;
import java.util.Set;
import org.thingsboard.common.model.EntityType;

public enum Resource {
	SYSTEM_SETTING(),
	MERCHANT(EntityType.MERCHANT),
	TENANT_PROFILE(EntityType.TENANT_PROFILE),
	TENANT(EntityType.TENANT),
	USER(EntityType.USER),
	OAUTH2_CONFIGURATION_INFO(),
	OAUTH2_CONFIGURATION_TEMPLATE(),
	VERSION_CONTROL,
	NOTIFICATION(EntityType.NOTIFICATION_TARGET, EntityType.NOTIFICATION_TEMPLATE,
		EntityType.NOTIFICATION_REQUEST, EntityType.NOTIFICATION_RULE)
	;

	private final Set<EntityType> entityTypes;

	Resource() {
		this.entityTypes = Collections.emptySet();
	}

	Resource(EntityType... entityTypes) {
		this.entityTypes = Set.of(entityTypes);
	}

	public Set<EntityType> getEntityTypes() {
		return entityTypes;
	}

	public static Resource of(EntityType entityType) {
		for (Resource resource : Resource.values()) {
			if (resource.getEntityTypes().contains(entityType)) {
				return resource;
			}
		}
		throw new IllegalArgumentException("Unknown EntityType: " + entityType.name());
	}
}
