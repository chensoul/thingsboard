package com.chensoul.system.domain.audit.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.audit.domain.ActionType;
import com.chensoul.system.domain.audit.domain.AuditLog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
//@SqlDao
@RequiredArgsConstructor
@Component
public class AuditLogDao extends AbstractDao<AuditLogEntity, AuditLog, Long> {
    private final AuditLogMapper mapper;

    protected Class<AuditLogEntity> getEntityClass() {
        return AuditLogEntity.class;
    }

    protected BaseMapper<AuditLogEntity> getRepository() {
        return mapper;
    }

    public PageData<AuditLog> findAuditLogsByTenantIdAndEntityId(String tenantId, String
        entityId, List<ActionType> actionTypes, PageLink pageLink) {
        return null;
    }

    public PageData<AuditLog> findAuditLogsByTenantIdAndMerchantId(String tenantId, Long
        merchantId, List<ActionType> actionTypes, PageLink pageLink) {
        return null;
    }

    public PageData<AuditLog> findAuditLogsByTenantIdAndUserId(String tenantId, Long
        userId, List<ActionType> actionTypes, PageLink pageLink) {
        return null;
    }

    public PageData<AuditLog> findAuditLogsByTenantId(String tenantId, List<ActionType> actionTypes, PageLink pageLink) {
        return null;
    }

    public void cleanUpAuditLogs(long expTime) {

    }
}
