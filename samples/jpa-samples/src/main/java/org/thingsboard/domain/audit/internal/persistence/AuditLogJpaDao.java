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
package org.thingsboard.domain.audit.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.audit.ActionType;
import org.thingsboard.domain.audit.AuditLog;

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
