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
package org.thingsboard.domain.merchant.internal.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, Long> {
	MerchantEntity findByTenantIdAndName(String tenantId, String name);

	void deleteByTenantId(String tenantId);

	@Query("SELECT c FROM MerchantEntity c WHERE c.tenantId = :tenantId " +
		   "AND (:textSearch IS NULL OR ilike(c.name, CONCAT('%', :textSearch, '%')) = true)")
	Page<MerchantEntity> findByTenantId(@Param("tenantId") String tenantId,
										@Param("textSearch") String textSearch,
										Pageable pageable);
}
