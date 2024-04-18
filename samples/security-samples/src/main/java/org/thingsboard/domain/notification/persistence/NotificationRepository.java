package org.thingsboard.domain.notification.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.domain.notification.NotificationStatus;
import org.thingsboard.domain.notification.template.NotificationDeliveryMethod;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

	@Transactional
	@Modifying
	@Query("DELETE FROM NotificationEntity n WHERE n.id = :id AND n.recipientId = :recipientId")
	int deleteByIdAndRecipientId(@Param("id") Long id, @Param("recipientId") Long recipientId);

	@Modifying
	@Transactional
	@Query("UPDATE NotificationEntity n SET n.status = :status " +
		   "WHERE n.id = :id AND n.recipientId = :recipientId AND n.status <> :status")
	int updateStatusByIdAndRecipientId(@Param("id") Long id,
									   @Param("recipientId") Long recipientId,
									   @Param("status") NotificationStatus status);

	@Modifying
	@Transactional
	@Query("UPDATE NotificationEntity n SET n.status = :status " +
		   "WHERE n.deliveryMethod = :deliveryMethod AND n.recipientId = :recipientId AND n.status <> :status")
	int updateStatusByDeliveryMethodAndRecipientIdAndStatusNot(@Param("deliveryMethod") NotificationDeliveryMethod deliveryMethod,
															   @Param("recipientId") UUID recipientId,
															   @Param("status") NotificationStatus status);

	@Query("SELECT n FROM NotificationEntity n WHERE n.deliveryMethod = :deliveryMethod " +
		   "AND n.recipientId = :recipientId AND n.status <> :status " +
		   "AND (:searchText is NULL OR ilike(n.subject, concat('%', :searchText, '%')) = true " +
		   "OR ilike(n.text, concat('%', :searchText, '%')) = true)")
	Page<NotificationEntity> findByDeliveryMethodAndRecipientIdAndStatusNot(
		@Param("deliveryMethod") NotificationDeliveryMethod deliveryMethod,
		@Param("recipientId") Long recipientId,
		@Param("status") NotificationStatus status,
		@Param("searchText") String searchText, Pageable pageable);


	@Query("SELECT n FROM NotificationEntity n WHERE n.deliveryMethod = :deliveryMethod AND n.recipientId = :recipientId " +
		   "AND (:searchText is NULL OR ilike(n.subject, concat('%', :searchText, '%')) = true " +
		   "OR ilike(n.text, concat('%', :searchText, '%')) = true)")
	Page<NotificationEntity> findByDeliveryMethodAndRecipientId(@Param("deliveryMethod") NotificationDeliveryMethod deliveryMethod,
																@Param("recipientId") Long recipientId,
																@Param("searchText") String searchText, Pageable pageable);
}
