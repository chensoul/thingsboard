package org.thingsboard.domain.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Serializable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.common.dao.DaoExecutorService;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.audit.sink.AuditLogSink;
import org.thingsboard.domain.user.model.User;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(prefix = "audit-log", value = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditLogServiceImpl implements AuditLogService {
	private final AuditLogDao auditLogDao;
	private final AuditLogLevelFilter auditLogLevelFilter;
	private final DaoExecutorService executor;
	private final AuditLogSink auditLogSink;
	private final AuditLogValidator auditLogValidator;

	@Override
	public Page<AuditLog> findAuditLogsByTenantIdAndMerchantId(Pageable pageable, String tenantId, Long merchantId, List<ActionType> actionTypes) {
		return auditLogDao.findAuditLogsByTenantIdAndMerchantId(pageable, tenantId, merchantId, actionTypes);
	}

	@Override
	public Page<AuditLog> findAuditLogsByTenantIdAndUserId(Pageable pageable, String tenantId, Long userId, List<ActionType> actionTypes) {
		return auditLogDao.findAuditLogsByTenantIdAndUserId(pageable, tenantId, userId, actionTypes);
	}

	@Override
	public Page<AuditLog> findAuditLogsByTenantIdAndEntityId(Pageable pageable, String tenantId, String entityId, List<ActionType> actionTypes) {
		return auditLogDao.findAuditLogsByTenantIdAndEntityId(pageable, tenantId, entityId, actionTypes);
	}

	@Override
	public Page<AuditLog> findAuditLogsByTenantId(Pageable pageable, String tenantId, List<ActionType> actionTypes) {
		return auditLogDao.findAuditLogsByTenantId(pageable, tenantId, actionTypes);
	}

	@Override
	public <E extends BaseData> ListenableFuture<Void> logEntityAction(User user, E entity, EntityType entityType, ActionType actionType, Exception e, JsonNode actionData) {
		return logEntityAction(user, entity, null, null, entityType, actionType, e, actionData);
	}

	@Override
	public <E extends BaseData> ListenableFuture<Void> logEntityAction(User user, E requestEntity, E oldEntity, E savedEntity, EntityType entityType, ActionType actionType, Exception e, JsonNode actionData) {
		if (canLog(entityType, actionType)) {
			return null;
		}

		E entity = savedEntity != null ? savedEntity : oldEntity;

		actionData = constructActionData(requestEntity, oldEntity, savedEntity, actionType, actionData);
		ActionStatus actionStatus = ActionStatus.SUCCESS;
		String failureDetail = "";
		if (e != null) {
			actionStatus = ActionStatus.FAILURE;
			failureDetail = getFailureStack(e);
		}
		return logAction(user, requestEntity, oldEntity, savedEntity, entityType, actionType, actionData, actionStatus, failureDetail);
	}

	private <E extends BaseData<Serializable>> ListenableFuture<Void> logAction(User user, E requestEntity, E oldEntity, E savedEntity, EntityType entityType, ActionType actionType, JsonNode actionData, ActionStatus actionStatus, String failureDetail) {
		AuditLog auditLog = new AuditLog();
		auditLog.setTenantId(user.getTenantId());
		auditLog.setMerchantId(user.getMerchantId());
		auditLog.setUserId(user.getId());
		auditLog.setUserName(user.getName());
		auditLog.setEntityId(getEntityId(requestEntity, oldEntity, savedEntity));
		auditLog.setEntityType(entityType);
		auditLog.setActionType(actionType);
		auditLog.setActionData(actionData);
		auditLog.setActionStatus(actionStatus);
		auditLog.setFailureDetail(failureDetail);

		try {
			auditLogValidator.validate(auditLog);
		} catch (Exception e) {
			return Futures.immediateFailedFuture(e);
		}

		return executor.submit(() -> {
			try {
				auditLogSink.logAction(auditLogDao.save(auditLog));
			} catch (Exception e) {
				log.error("Failed to save audit log: {}", auditLog, e);
			}
			return null;
		});
	}

	private <E extends BaseData<Serializable>> String getEntityId(E... entities) {
		for (E entity : entities) {
			if (entity != null && entity.getId() != null) {
				return String.valueOf(entity.getId());
			}
		}
		return null;
	}

	private <E extends BaseData> JsonNode constructActionData(E requestEntity, E oldEntity, E savedEntity, ActionType actionType, JsonNode actionData) {
		ObjectNode newObjectNode = JacksonUtil.newObjectNode();
		if (actionData != null) {
			newObjectNode.putAll((ObjectNode) actionData);
		}

		switch (actionType) {
			case ADD:
			case UPDATE:
				newObjectNode.put("param", JacksonUtil.toString(requestEntity));

				if (oldEntity != null) {
					newObjectNode.put("oldEntity", JacksonUtil.toString(oldEntity));
				}
				if (savedEntity != null) {
					newObjectNode.put("newEntity", JacksonUtil.toString(savedEntity));
				}
				break;
			case DELETE:
				if (oldEntity != null) {
					newObjectNode.put("oldEntity", JacksonUtil.toString(oldEntity));
				}
				break;
			case LOGIN:
			case LOGOUT:
			case LOCKOUT:
				break;
		}

		return newObjectNode;
	}

	private boolean canLog(EntityType entityType, ActionType actionType) {
		return auditLogLevelFilter.logEnabled(entityType, actionType);
	}

	private String getFailureStack(Exception e) {
		return e.getMessage();
	}

}
