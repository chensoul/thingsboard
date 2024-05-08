package org.thingsboard.domain.notification.internal.persistence;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.domain.notification.internal.template.NotificationType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, Long> {
	void deleteByTenantId(String tenantId);

	@Query("SELECT t FROM NotificationTemplateEntity t WHERE t.tenantId = :tenantId AND " +
		   "t.type IN :notificationTypes " +
		   "AND (:searchText is NULL OR ilike(t.name, concat('%', :searchText, '%')) = true " +
		   "OR ilike(t.type, concat('%', :searchText, '%')) = true)")
	Page<NotificationTemplateEntity> findByTenantIdAndNotificationTypesAndSearchText(Pageable pageable,
																					 @Param("tenantId") String tenantId,
																					 @Param("notificationTypes") List<NotificationType> notificationTypes,
																					 @Param("searchText") String searchText);
}
