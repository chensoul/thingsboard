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
package org.thingsboard.domain.notificationrule.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.domain.notificationrule.NotificationRule;
import org.thingsboard.domain.notificationrule.NotificationRuleTriggerType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class NotificationRuleJpaDao extends JpaAbstractDao<NotificationRuleEntity, NotificationRule, Long> implements NotificationRuleDao {

	private final NotificationRuleRepository notificationRuleRepository;

	@Override
	protected Class<NotificationRuleEntity> getEntityClass() {
		return NotificationRuleEntity.class;
	}

	@Override
	protected JpaRepository<NotificationRuleEntity, Long> getRepository() {
		return notificationRuleRepository;
	}

	@Override
	public List<NotificationRule> findByTenantIdAndTriggerTypeAndEnabled(String tenantId, NotificationRuleTriggerType triggerType, boolean enabled) {
		return List.of();
	}
}
