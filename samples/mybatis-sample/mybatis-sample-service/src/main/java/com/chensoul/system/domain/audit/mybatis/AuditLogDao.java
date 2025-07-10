/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
