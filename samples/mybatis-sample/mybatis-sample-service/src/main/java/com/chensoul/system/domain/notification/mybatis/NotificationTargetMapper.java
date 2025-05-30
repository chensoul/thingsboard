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
package com.chensoul.system.domain.notification.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface NotificationTargetMapper extends BaseMapper<NotificationTargetEntity> {
    void deleteByTenantId(String tenantId);

    Long countByTenantId(String tenantId);

    List<NotificationTargetEntity> findByTenantIdAndIdIn(String tenantId, Set<Long> ids);

    //    @Query(value = "SELECT * FROM notification_target t WHERE t.tenant_id = :tenantId " +
//                   "AND (:searchText IS NULL OR t.name ILIKE concat('%', :searchText, '%')) " +
//                   "AND (cast(t.configuration as json) ->> 'type' <> 'PLATFORM_USERS' OR " +
//                   "cast(t.configuration as json) -> 'usersFilter' ->> 'type' IN :usersFilterTypes)", nativeQuery = true)
    Page<NotificationTargetEntity> findByTenantIdAndSearchTextAndUsersFilterTypeIfPresent(@Param("tenantId") String tenantId,
                                                                                          @Param("usersFilterTypes") List<String> usersFilterTypes,
                                                                                          @Param("searchText") String searchText,
                                                                                          Pageable pageable);

}
