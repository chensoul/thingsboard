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
