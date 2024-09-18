package org.thingsboard.domain.tenant.internal.persistence;

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
public interface TenantRepository extends JpaRepository<TenantEntity, String> {
	TenantEntity findByName(String name);

	@Query("SELECT t FROM TenantEntity t WHERE (:textSearch IS NULL OR ilike(t.name, CONCAT('%', :textSearch, '%')) = true)")
	Page<TenantEntity> findTenants(Pageable pageable, @Param("textSearch") String textSearch);
}
