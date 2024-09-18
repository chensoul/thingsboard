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
public interface TenantProfileRepository extends JpaRepository<TenantProfileEntity, Long> {
	@Query("SELECT t FROM TenantProfileEntity t " +
		   "WHERE t.defaulted = true")
	TenantProfileEntity findByDefaultTrue();

	@Query("SELECT t FROM TenantProfileEntity t WHERE " +
		   "(:textSearch IS NULL OR ilike(t.name, CONCAT('%', :textSearch, '%')) = true)")
	Page<TenantProfileEntity> findTenantProfiles(@Param("textSearch") String textSearch, Pageable pageable);

}
