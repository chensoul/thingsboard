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
package com.chensoul.system.domain.notificationrule.internal.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.system.domain.notificationrule.NotificationRuleTriggerType;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Mapper
public interface NotificationRuleRepository extends BaseMapper<NotificationRuleEntity> {
    //	@Query("SELECT r FROM NotificationRuleEntity r WHERE r.tenantId = :tenantId " +
//		   "AND (:searchText is NULL OR ilike(r.name, concat('%', :searchText, '%')) = true)")
    Page<NotificationRuleEntity> findByTenantIdAndSearchText(@Param("tenantId") String tenantId,
                                                             @Param("searchText") String searchText,
                                                             Pageable pageable);

    //	@Query("SELECT count(r) > 0 FROM NotificationRuleEntity r WHERE r.tenantId = :tenantId " +
//		   "AND CAST(r.recipientsConfig AS text) LIKE concat('%', :searchString, '%')")
    boolean existsByTenantIdAndRecipientsConfigContaining(@Param("tenantId") String tenantId,
                                                          @Param("searchString") String searchString);

    List<NotificationRuleEntity> findAllByTenantIdAndTriggerTypeAndEnabled(String tenantId, NotificationRuleTriggerType triggerType, boolean enabled);

    @Transactional
//	@Modifying
//	@Query("DELETE FROM NotificationRuleEntity r WHERE r.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") String tenantId);

    NotificationRuleEntity findByTenantIdAndName(String tenantId, String name);

    Page<NotificationRuleEntity> findByTenantId(String tenantId, Pageable pageable);
}
