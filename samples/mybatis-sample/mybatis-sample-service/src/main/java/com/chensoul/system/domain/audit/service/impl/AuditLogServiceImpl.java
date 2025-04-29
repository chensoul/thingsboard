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
package com.chensoul.system.domain.audit.service.impl;

import com.chensoul.constant.StringPool;
import static com.chensoul.constant.WebConstants.HEADER_CLIENT_ID;
import static com.chensoul.constant.WebConstants.HEADER_TENANT_ID;
import static com.chensoul.constant.WebConstants.HEADER_USER_AGENT;
import static com.chensoul.constant.WebConstants.TRACE_ID;
import com.chensoul.data.model.HasId;
import com.chensoul.data.model.HasName;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.exception.BusinessException;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.extend.tenant.TenantContextHolder;
import com.chensoul.spring.support.SpringContextHolder;
import com.chensoul.spring.util.ServletUtils;
import com.chensoul.system.domain.audit.configuration.AuditLogLevelConfiguration;
import com.chensoul.system.domain.audit.domain.ActionStatus;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.AuditLog;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.audit.mybatis.AuditLogDao;
import com.chensoul.system.domain.audit.service.AuditLogService;
import com.chensoul.system.domain.audit.sink.AuditLogSink;
import com.chensoul.system.infrastructure.common.EntityService;
import com.chensoul.system.infrastructure.executor.DaoExecutorService;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getSecurityUser;
import com.chensoul.system.user.domain.User;
import static com.chensoul.web.webmvc.WebMvcConfiguration.LogSlowResponseTimeFilter.EXEC_TIME;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuditLogServiceImpl implements AuditLogService {
    public static final String[] SENSTIVE_FIELDS = {"password", "currentPassword", "newPassword"};
    private final AuditLogDao auditLogDao;
    private final AuditLogLevelConfiguration auditLogLevelConfiguration;
    private final DaoExecutorService executor;
    private final AuditLogSink auditLogSink;
    private final EntityService entityService;

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
    public <E extends Serializable> ListenableFuture<Void> logEntityAction(User user, E requestEntity, EntityType entityType, ActionType actionType, Throwable e, JsonNode actionData) {
        return logEntityAction(user, requestEntity, null, null, entityType, actionType, e, actionData);
    }

    @Override
    public <E extends Serializable> ListenableFuture<Void> logEntityAction(User user, E requestEntity, E oldEntity, Object savedEntity, EntityType entityType, ActionType actionType, Throwable e, JsonNode actionData) {
        if (!canLog(entityType, actionType)) {
            return null;
        }

        actionData = constructActionData(requestEntity, oldEntity, savedEntity, actionType, actionData);
        ActionStatus actionStatus = ActionStatus.SUCCESS;
        String failureDetail = "";
        if (e != null) {
            actionStatus = ActionStatus.FAILURE;
            failureDetail = ExceptionUtils.getRootCauseMessage(e);
        }

        AtomicLong costTime = new AtomicLong();
        ServletUtils.getHttpServletRequest().ifPresent(t -> {
            HttpServletRequest request = ServletUtils.getHttpServletRequest().get();
            Long startTime = (Long) request.getAttribute(EXEC_TIME);
            if (costTime != null) {
                costTime.set(System.currentTimeMillis() - startTime);
            }
        });

        return logAction(user, requestEntity, oldEntity, savedEntity, entityType, actionType, actionData, actionStatus, failureDetail, costTime.get());
    }

    public <E extends Serializable> Object doAndLog(E requestEntity, @NotNull E oldEntity, EntityType entityType, ActionType actionType, Function<E, ?> function) {
        if (log.isDebugEnabled()) {
            log.debug("Enter doAndLog: {} with requestEntity = {}", entityType.name(), JacksonUtils.toString(requestEntity));
        }

        if (requestEntity == null && oldEntity == null) {
            throw new BusinessException("requestEntity和oldEntity不能都为空");
        }

        Object savedEntity = null;
        Exception exception = null;
        try {
            savedEntity = function.apply(requestEntity);
            return savedEntity;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            logEntityAction(getSecurityUser(), requestEntity, oldEntity, savedEntity, entityType, actionType, exception, null);
        }
    }

    @Override
    public <E extends Serializable> void doAndLog(E requestEntity, EntityType entityType, ActionType actionType, Consumer<E> consumer) {
        if (log.isDebugEnabled()) {
            log.debug("Enter doAndLog: {} with requestEntity = {}", entityType.name(), JacksonUtils.toString(requestEntity));
        }

        Exception exception = null;
        try {
            consumer.accept(requestEntity);
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            logEntityAction(getSecurityUser(), requestEntity, null, null, entityType, actionType, exception, null);
        }
    }

    private <E extends Serializable> ListenableFuture<Void> logAction(User user, E requestEntity, E oldEntity, Object savedEntity,
                                                                      EntityType entityType, ActionType actionType, JsonNode actionData,
                                                                      ActionStatus actionStatus, String failureDetail, long costTime) {
        AuditLog auditLog = new AuditLog();
        auditLog.setServiceId(SpringContextHolder.getApplicationName());
        auditLog.setCreateTime(LocalDateTime.now());
        auditLog.setTraceId(MDC.get(TRACE_ID));

        ServletUtils.getHttpServletRequest().ifPresent(t -> {
            auditLog.setRequestUri(ServletUtils.getHttpServletRequest().get().getRequestURI());
            auditLog.setUserIp(ServletUtils.getClientIp());
            auditLog.setUserAgent(ServletUtils.getValueFromRequest(HEADER_USER_AGENT));
            auditLog.setClientId(ServletUtils.getValueFromRequest(HEADER_CLIENT_ID));
            auditLog.setTenantId(ServletUtils.getValueFromRequest(HEADER_TENANT_ID));
        });

        if (StringUtils.isBlank(auditLog.getTenantId())) {
            auditLog.setTenantId(TenantContextHolder.getTenantId());
        }

        if (user != null) {
            auditLog.setMerchantId(user.getMerchantId());
            auditLog.setUserId(user.getId());
            auditLog.setUserName(user.getName());
            auditLog.setTenantId(user.getTenantId());
        }

        Serializable entityId = getEntityId(requestEntity, oldEntity, savedEntity);
        String entityName = getEntityName(savedEntity, entityType, entityId);

        auditLog.setEntityId(entityId != null ? String.valueOf(entityId) : null);
        auditLog.setEntityName(entityName);
        auditLog.setEntityType(entityType);
        auditLog.setActionType(actionType);
        auditLog.setExtra(actionData);
        auditLog.setActionStatus(actionStatus);
        auditLog.setFailureDetail(failureDetail);
        auditLog.setCostTime(costTime);

        return executor.submit(() -> {
            try {
                auditLogSink.logAction(auditLogDao.save(auditLog));
            } catch (Exception e) {
                log.error("Failed to save audit log: {}", auditLog, e);
            }
            return null;
        });
    }

    private String getEntityName(Object savedEntity, EntityType entityType, Serializable entityId) {
        String entityName = "N/A";
        if (savedEntity != null && savedEntity instanceof HasName) {
            entityName = ((HasName) savedEntity).getName();
        } else if (entityId != null) {
            try {
                entityName = entityService.fetchEntityName(entityType, entityId).orElse(entityName);
            } catch (Exception ignored) {
                //
                log.error("", ignored);
            }
        }
        return entityName;
    }

    private Serializable getEntityId(Object... entities) {
        for (Object entity : entities) {
            if (entity != null && entity instanceof HasId) {
                return ((HasId<?>) entity).getId();
            }
        }
        return null;
    }

    private <E extends Serializable> JsonNode constructActionData(E requestEntity, E oldEntity, Object savedEntity, ActionType actionType, JsonNode actionData) {
        ObjectNode newObjectNode = JacksonUtils.newObjectNode();
        if (actionData != null) {
            newObjectNode.putAll((ObjectNode) actionData);
        }

        switch (actionType) {
            case ADD:
            case UPDATE:
            case DELETE:
                saveRequest(newObjectNode);
                if (oldEntity != null) {
                    newObjectNode.put("oldEntity", JacksonUtils.toString(oldEntity));
                }
                if (savedEntity != null && !ObjectUtils.equals(oldEntity, savedEntity)) {
                    newObjectNode.put("newEntity", JacksonUtils.toString(savedEntity));
                }
                break;
            case CREDENTIAL_UPDATE:
            case LOGIN:
            case LOCKOUT:
            case LOGOUT:
                saveRequest(newObjectNode);
                break;
        }

        return newObjectNode;
    }

    private void saveRequest(ObjectNode newObjectNode) {
        ServletUtils.getHttpServletRequest().ifPresent(t -> {
            HttpServletRequest request = ServletUtils.getHttpServletRequest().get();
            if (StringUtils.isNotBlank(request.getQueryString())) {
                newObjectNode.put("params", request.getQueryString());
            }
            if (ServletUtils.isJsonRequest(request)) {
                String payload = ServletUtils.getMessagePayload(request);
                if (StringUtils.isNotBlank(payload)) {
                    try {
                        JsonNode jsonNode = JacksonUtils.toJsonNode(payload);
                        if (jsonNode != null && jsonNode instanceof ObjectNode) {
                            for (String key : SENSTIVE_FIELDS) {
                                if (jsonNode.has(key)) {
                                    ((ObjectNode) jsonNode).put(key, StringPool.STAR_THREE);
                                }
                            }
                            payload = JacksonUtils.toString(jsonNode);
                        }
                    } catch (Exception e) {
                    }
                }
                if (StringUtils.isNotBlank(payload)) {
                    newObjectNode.put("payload", payload);
                }
            }
        });
    }

    private boolean canLog(EntityType entityType, ActionType actionType) {
        return auditLogLevelConfiguration.logEnabled(entityType, actionType);
    }
}
