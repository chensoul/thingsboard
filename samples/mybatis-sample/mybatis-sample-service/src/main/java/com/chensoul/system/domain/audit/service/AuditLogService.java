package com.chensoul.system.domain.audit.service;

import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.AuditLog;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.user.domain.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface AuditLogService {
    PageData<AuditLog> findAuditLogsByTenantIdAndMerchantId(String tenantId, Long merchantId, List<ActionType> actionTypes, PageLink pageLink);

    PageData<AuditLog> findAuditLogsByTenantIdAndUserId(String tenantId, Long userId, List<ActionType> actionTypes, PageLink pageLink);

    PageData<AuditLog> findAuditLogsByTenantIdAndEntityId(String tenantId, String entityId, List<ActionType> actionTypes, PageLink pageLink);

    PageData<AuditLog> findAuditLogsByTenantId(String tenantId, List<ActionType> actionTypes, PageLink pageLink);

    <E extends Serializable> ListenableFuture<Void> logEntityAction(
        User user,
        E entity,
        EntityType entityType,
        ActionType actionType,
        Throwable e, JsonNode actionData);

    <E extends Serializable> ListenableFuture<Void> logEntityAction(
        User user,
        E requestEntity,
        E oldEntity, Object savedEntity,
        EntityType entityType,
        ActionType actionType,
        Throwable e, JsonNode actionData);

    <E extends Serializable> Object doAndLog(E requestEntity, E oldEntity, EntityType entityType, ActionType actionType, Function<E, ?> savingFunction);

    <E extends Serializable> void doAndLog(E requestEntity, EntityType entityType, ActionType actionType, Consumer<E> consumer);
}
