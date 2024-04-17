package org.thingsboard.domain.audit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MybatisAuditLogDao extends AbstractMybatisDao<AuditLogEntity, AuditLog> implements AuditLogDao {
	private final AuditLogMapper mapper;

	@Override
	protected Class<AuditLogEntity> getEntityClass() {
		return AuditLogEntity.class;
	}

	@Override
	protected BaseMapper<AuditLogEntity> getRepository() {
		return mapper;
	}

	@Override
	public Page<AuditLog> findAuditLogsByTenantIdAndEntityId(Pageable pageable, String tenantId, String entityId, List<ActionType> actionTypes) {
		return null;
	}

	@Override
	public Page<AuditLog> findAuditLogsByTenantIdAndMerchantId(Pageable pageable, String tenantId, Long merchantId, List<ActionType> actionTypes) {
		return null;
	}

	@Override
	public Page<AuditLog> findAuditLogsByTenantIdAndUserId(Pageable pageable, String tenantId, Long userId, List<ActionType> actionTypes) {
		return null;
	}

	@Override
	public Page<AuditLog> findAuditLogsByTenantId(Pageable pageable, String tenantId, List<ActionType> actionTypes) {
		return null;
	}

	@Override
	public void cleanUpAuditLogs(long expTime) {

	}
}
