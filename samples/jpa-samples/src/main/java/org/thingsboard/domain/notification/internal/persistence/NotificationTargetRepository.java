package org.thingsboard.domain.notification.internal.persistence;

import java.util.List;
import java.util.Set;
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
public interface NotificationTargetRepository extends JpaRepository<NotificationTargetEntity, Long> {
	void deleteByTenantId(String tenantId);

	Long countByTenantId(String tenantId);

	List<NotificationTargetEntity> findByTenantIdAndIdIn(String tenantId, Set<Long> ids);

	@Query(value = "SELECT * FROM notification_target t WHERE t.tenant_id = :tenantId " +
				   "AND (:searchText IS NULL OR t.name ILIKE concat('%', :searchText, '%')) " +
				   "AND (cast(t.configuration as json) ->> 'type' <> 'PLATFORM_USERS' OR " +
				   "cast(t.configuration as json) -> 'usersFilter' ->> 'type' IN :usersFilterTypes)", nativeQuery = true)
	Page<NotificationTargetEntity> findByTenantIdAndSearchTextAndUsersFilterTypeIfPresent(@Param("tenantId") String tenantId,
																						  @Param("usersFilterTypes") List<String> usersFilterTypes,
																						  @Param("searchText") String searchText,
																						  Pageable pageable);

}
