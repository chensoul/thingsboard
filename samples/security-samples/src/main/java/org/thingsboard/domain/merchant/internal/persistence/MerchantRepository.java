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
