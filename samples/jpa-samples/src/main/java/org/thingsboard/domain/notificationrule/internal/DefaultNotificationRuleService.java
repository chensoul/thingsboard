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
package org.thingsboard.domain.notificationrule.internal;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasId;
import org.thingsboard.data.dao.EntityDaoService;
import org.thingsboard.domain.notificationrule.NotificationRule;
import org.thingsboard.domain.notificationrule.NotificationRuleService;
import org.thingsboard.domain.notificationrule.internal.persistence.NotificationRuleDao;
import org.thingsboard.domain.notificationrule.NotificationRuleTriggerType;

@Service
@RequiredArgsConstructor
public class DefaultNotificationRuleService implements NotificationRuleService, EntityDaoService {

	private final NotificationRuleDao notificationRuleDao;


	@Override
	public Optional<HasId> findEntity(Object id) {
		return Optional.empty();
	}

	@Override
	public EntityType getEntityType() {
		return null;
	}

	@Override
	public List<NotificationRule> findEnabledNotificationRulesByTenantIdAndTriggerType(String tenantId, NotificationRuleTriggerType triggerType) {
		return List.of();
	}
}
