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
package org.thingsboard.domain.notification.rule;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.domain.notification.rule.trigger.config.NotificationRuleTriggerType;

@Repository
public interface NotificationRuleRepository extends JpaRepository<NotificationRuleEntity, Long> {
	@Query("SELECT r FROM NotificationRuleEntity r WHERE r.tenantId = :tenantId " +
		   "AND (:searchText is NULL OR ilike(r.name, concat('%', :searchText, '%')) = true)")
	Page<NotificationRuleEntity> findByTenantIdAndSearchText(@Param("tenantId") String tenantId,
															 @Param("searchText") String searchText,
															 Pageable pageable);

	@Query("SELECT count(r) > 0 FROM NotificationRuleEntity r WHERE r.tenantId = :tenantId " +
		   "AND CAST(r.recipientsConfig AS text) LIKE concat('%', :searchString, '%')")
	boolean existsByTenantIdAndRecipientsConfigContaining(@Param("tenantId") String tenantId,
														  @Param("searchString") String searchString);

	List<NotificationRuleEntity> findAllByTenantIdAndTriggerTypeAndEnabled(String tenantId, NotificationRuleTriggerType triggerType, boolean enabled);

	@Transactional
	@Modifying
	@Query("DELETE FROM NotificationRuleEntity r WHERE r.tenantId = :tenantId")
	void deleteByTenantId(@Param("tenantId") String tenantId);

	NotificationRuleEntity findByTenantIdAndName(String tenantId, String name);

	Page<NotificationRuleEntity> findByTenantId(String tenantId, Pageable pageable);
}
