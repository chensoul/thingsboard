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
package org.thingsboard.domain.notification.targets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
	@Type(value = AllUserFilter.class, name = "ALL_USER"),
	@Type(value = UserListFilter.class, name = "USER_LIST"),
	@Type(value = MerchantUserFilter.class, name = "MERCHANT_USER"),
	@Type(value = TenantAdminFilter.class, name = "TENANT_ADMIN"),
	@Type(value = TenantUserFilter.class, name = "TENANT_USER"),
	@Type(value = AffectedTenantAdminFilter.class, name = "AFFECTED_TENANT_ADMIN"),
	@Type(value = SystemAdminFilter.class, name = "SYSTEM_ADMIN"),
	@Type(value = OriginatorEntityOwnerUserFilter.class, name = "ORIGINATOR_ENTITY_OWNER_USER"),
	@Type(value = AffectedUserFilter.class, name = "AFFECTED_USER")
})
public interface UserFilter {

	@JsonIgnore
	UserFilterType getType();

}
