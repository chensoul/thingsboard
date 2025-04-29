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
//package org.thingsboard.domain.audit;
//
//import com.google.common.util.concurrent.ListenableFuture;
//import java.io.Serializable;
//import java.util.List;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.thingsboard.common.model.EntityType;
//import org.thingsboard.common.model.HasName;
//
///**
// * TODO Comment
// *
// * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
// * @since TODO
// */
//@Service
//@ConditionalOnProperty(prefix = "audit-log", value = "enabled", havingValue = "false")
//public class DummyAuditLogServiceImpl implements AuditLogService {
//	@Override
//	public Page<AuditLog> findAuditLogsByTenantIdAndMerchantId(Pageable pageable, String tenantId, Long merchantId, List<ActionType> actionTypes) {
//		return null;
//	}
//
//	@Override
//	public Page<AuditLog> findAuditLogsByTenantIdAndUserId(Pageable pageable, String tenantId, Long userId, List<ActionType> actionTypes) {
//		return null;
//	}
//
//	@Override
//	public Page<AuditLog> findAuditLogsByTenantIdAndEntityId(Pageable pageable, String tenantId, String entityId, List<ActionType> actionTypes) {
//		return null;
//	}
//
//	@Override
//	public Page<AuditLog> findAuditLogsByTenantId(Pageable pageable, String tenantId, List<ActionType> actionTypes) {
//		return null;
//	}
//
//	@Override
//	public <E extends HasName, I extends Serializable> ListenableFuture<Void> logEntityAction(String tenantId, Long merchantId, Long userId, String userName, EntityType entityType, ActionType actionType, Exception e, Object... additionalInfo) {
//		return null;
//	}
//}
