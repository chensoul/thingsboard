package org.thingsboard.domain.audit;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.JpaAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.jpa.PageData;
import org.thingsboard.common.dao.jpa.PageLink;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class AuditLogJpaDao extends JpaAbstractDao<AuditLogEntity, AuditLog, Long> implements AuditLogDao {
	private final AuditLogRepository repository;

	@Override
	protected Class<AuditLogEntity> getEntityClass() {
		return AuditLogEntity.class;
	}

	@Override
	protected JpaRepository<AuditLogEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public PageData<AuditLog> findAuditLogsByTenantIdAndEntityId(String tenantId, String entityId, List<ActionType> actionTypes, PageLink pageLink) {
		return null;
	}

	@Override
	public PageData<AuditLog> findAuditLogsByTenantIdAndMerchantId(String tenantId, Long merchantId, List<ActionType> actionTypes, PageLink pageLink) {
		return null;
	}

	@Override
	public PageData<AuditLog> findAuditLogsByTenantIdAndUserId(String tenantId, Long userId, List<ActionType> actionTypes, PageLink pageLink) {
		return null;
	}

	@Override
	public PageData<AuditLog> findAuditLogsByTenantId(String tenantId, List<ActionType> actionTypes, PageLink pageLink) {
		return null;
	}

	@Override
	public void cleanUpAuditLogs(long expTime) {

	}
}
