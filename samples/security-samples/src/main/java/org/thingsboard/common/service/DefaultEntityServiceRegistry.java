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
package org.thingsboard.common.service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thingsboard.common.dao.EntityDaoService;
import org.thingsboard.common.model.EntityType;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultEntityServiceRegistry implements EntityServiceRegistry {
	private final List<EntityDaoService> entityDaoServices;
	private final Map<EntityType, EntityDaoService> entityServicesMap = new HashMap<>();

	@PostConstruct
	public void init() {
		log.debug("Initializing EntityServiceRegistry on ContextRefreshedEvent");
		entityDaoServices.forEach(entityDaoService -> {
			EntityType entityType = entityDaoService.getEntityType();
			entityServicesMap.put(entityType, entityDaoService);
		});
		log.debug("Initialized EntityServiceRegistry total [{}] entries", entityServicesMap.size());
	}

	@Override
	public EntityDaoService getServiceByEntityType(EntityType entityType) {
		return entityServicesMap.get(entityType);
	}

}
