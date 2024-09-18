package org.thingsboard.domain.audit.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
}
