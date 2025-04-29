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
