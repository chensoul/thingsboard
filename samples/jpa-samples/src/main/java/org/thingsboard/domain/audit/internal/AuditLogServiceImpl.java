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
package org.thingsboard.domain.audit.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Serializable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.thingsboard.data.dao.DaoExecutorService;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.common.model.BaseData;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.audit.ActionStatus;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.audit.AuditLog;
import org.thingsboard.domain.audit.AuditLogService;
import org.thingsboard.domain.audit.internal.config.AuditLogLevelConfiguration;
import org.thingsboard.domain.audit.internal.persistence.AuditLogDao;
import org.thingsboard.domain.audit.internal.sink.AuditLogSink;
import org.thingsboard.domain.setting.ServiceInfoProvider;
import org.thingsboard.domain.user.User;

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
	private final AuditLogLevelConfiguration auditLogLevelConfiguration;
	private final DaoExecutorService executor;
	private final AuditLogSink auditLogSink;
	private final AuditLogValidator auditLogValidator;
	private final ServiceInfoProvider serviceInfoProvider;

	@Override
	public PageData<AuditLog> findAuditLogsByTenantIdAndMerchantId(String tenantId, Long merchantId, List<ActionType> actionTypes, PageLink pageLink) {
		return auditLogDao.findAuditLogsByTenantIdAndMerchantId(tenantId, merchantId, actionTypes, pageLink);
	}

	@Override
	public PageData<AuditLog> findAuditLogsByTenantIdAndUserId(String tenantId, Long userId, List<ActionType> actionTypes, PageLink pageLink) {
		return auditLogDao.findAuditLogsByTenantIdAndUserId(tenantId, userId, actionTypes, pageLink);
	}

	@Override
	public PageData<AuditLog> findAuditLogsByTenantIdAndEntityId(String tenantId, String entityId, List<ActionType> actionTypes, PageLink pageLink) {
		return auditLogDao.findAuditLogsByTenantIdAndEntityId(tenantId, entityId, actionTypes, pageLink);
	}

	@Override
	public PageData<AuditLog> findAuditLogsByTenantId(String tenantId, List<ActionType> actionTypes, PageLink pageLink) {
		return auditLogDao.findAuditLogsByTenantId(tenantId, actionTypes, pageLink);
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
		auditLog.setServiceId(serviceInfoProvider.getServiceId());
		auditLog.setServiceName(serviceInfoProvider.getServiceName());
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
		return auditLogLevelConfiguration.logEnabled(entityType, actionType);
	}

	private String getFailureStack(Exception e) {
		return e.getMessage();
	}

}
