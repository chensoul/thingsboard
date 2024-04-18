package org.thingsboard.domain.user.persistence;

import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.domain.user.model.Authority;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	UserEntity findByEmail(String email);

	Long countByTenantId(String tenantId);

	@Query("SELECT u FROM UserEntity u WHERE u.tenantId IN (:tenantIds)  AND u.authority = :authority " +
		   "AND (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
	Page<UserEntity> findByTenantIdsAndAuthority(@Param("tenantIds") Set<String> tenantIds,
												 @Param("authority") Authority authority,
												 @Param("searchText") String searchText,
												 Pageable pageable);

	@Query("SELECT u FROM UserEntity u WHERE u.authority = :authority " +
		   "And (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
	Page<UserEntity> findByAuthority(@Param("authority") Authority authority,
									 @Param("searchText") String searchText,
									 Pageable pageable);

	@Query("SELECT u FROM UserEntity u WHERE u.tenantId = :tenantId " +
		   "AND (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
	Page<UserEntity> findByTenantId(
		@Param("tenantId") String tenantId,
		@Param("searchText") String searchText, Pageable pageable);

	@Query("SELECT u FROM UserEntity u WHERE u.merchantId in (:merchantIds) " +
		   "AND (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
	Page<UserEntity> findByMerchantIds(@Param("merchantIds") Set<Long> merchantIds,
									   @Param("searchText") String searchText, Pageable pageable);

	@Query("SELECT u FROM UserEntity u WHERE u.id IN (:ids) " +
		   "AND (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
	Page<UserEntity> findByIds(Set<Long> ids, String searchText, Pageable pageable);

	@Query("SELECT u FROM UserEntity u WHERE (:searchText IS NULL OR ilike(u.email, CONCAT('%', :searchText, '%')) = true)")
	Page<UserEntity> find(String searchText, Pageable pageable);
}
