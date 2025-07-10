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
package com.chensoul.system.infrastructure.common;

import com.chensoul.data.model.HasId;
import com.chensoul.data.model.HasName;
import com.chensoul.system.domain.audit.domain.EntityType;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BaseEntityService implements EntityService {
    @Autowired
    @Lazy
    EntityServiceRegistry entityServiceRegistry;

    @Override
    public Optional<String> fetchEntityName(EntityType entityType, Serializable entityId) {
        log.trace("Executing fetchEntityName [{}]", entityId);
        return fetchAndConvert(entityType, entityId, this::getName);
    }

    private <T> Optional<T> fetchAndConvert(EntityType entityType, Serializable entityId, Function<HasId, T> converter) {
        EntityDaoService entityDaoService = entityServiceRegistry.getServiceByEntityType(entityType);
        if (entityDaoService == null) {
            return Optional.empty();
        }
        Optional<HasId> entityOpt = entityDaoService.findEntity(entityId);
        return entityOpt.map(converter);
    }

    private String getName(HasId<?> entity) {
        return entity instanceof HasName ? ((HasName) entity).getName() : null;
    }
}
