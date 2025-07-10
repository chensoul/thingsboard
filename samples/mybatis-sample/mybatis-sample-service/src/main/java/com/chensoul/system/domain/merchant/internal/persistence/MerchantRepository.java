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
package com.chensoul.system.domain.merchant.internal.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface MerchantRepository extends BaseMapper<MerchantEntity> {
    MerchantEntity findByTenantIdAndName(String tenantId, String name);

    void deleteByTenantId(String tenantId);

    //    @Query("SELECT c FROM MerchantEntity c WHERE c.tenantId = :tenantId " +
//           "AND (:textSearch IS NULL OR ilike(c.name, CONCAT('%', :textSearch, '%')) = true)")
    Page<MerchantEntity> findByTenantId(@Param("tenantId") String tenantId,
                                        @Param("textSearch") String textSearch,
                                        Pageable pageable);
}
