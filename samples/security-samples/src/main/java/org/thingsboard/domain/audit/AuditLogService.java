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
package org.thingsboard.domain.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.user.model.User;

public interface AuditLogService {

	Page<AuditLog> findAuditLogsByTenantIdAndMerchantId(Pageable pageable, String tenantId, Long merchantId, List<ActionType> actionTypes);

	Page<AuditLog> findAuditLogsByTenantIdAndUserId(Pageable pageable, String tenantId, Long userId, List<ActionType> actionTypes);

	Page<AuditLog> findAuditLogsByTenantIdAndEntityId(Pageable pageable, String tenantId, String entityId, List<ActionType> actionTypes);

	Page<AuditLog> findAuditLogsByTenantId(Pageable pageable, String tenantId, List<ActionType> actionTypes);

	<E extends BaseData> ListenableFuture<Void> logEntityAction(
		User user,
		E entity,
		EntityType entityType,
		ActionType actionType,
		Exception e, JsonNode actionData);

	<E extends BaseData> ListenableFuture<Void> logEntityAction(
		User user,
		E requestEntity,
		E oldEntity, E savedEntity,
		EntityType entityType,
		ActionType actionType,
		Exception e, JsonNode actionData);

}
