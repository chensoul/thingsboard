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
package org.thingsboard.domain.notification.internal.persistence;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.notification.internal.targets.NotificationTarget;
import org.thingsboard.domain.notification.internal.targets.NotificationTargetType;
import org.thingsboard.domain.notification.internal.targets.UserFilterType;
import org.thingsboard.domain.notification.internal.template.NotificationType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@AllArgsConstructor
@Component
public class NotificationTargetJpaDao extends JpaAbstractDao<NotificationTargetEntity, NotificationTarget, Long> implements NotificationTargetDao {
	private NotificationTargetRepository repository;

	@Override
	protected Class<NotificationTargetEntity> getEntityClass() {
		return NotificationTargetEntity.class;
	}

	@Override
	protected JpaRepository<NotificationTargetEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public List<NotificationTarget> findByTenantIdAndIds(String tenantId, Set<Long> ids) {
		return DaoUtil.convertDataList(repository.findByTenantIdAndIdIn(tenantId, ids));
	}

	@Override
	public List<NotificationTarget> findByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType) {
		return List.of();
	}

	@Override
	public PageData<NotificationTarget> findByTenantIdAndSupportedNotificationType(String tenantId, NotificationTargetType notificationTargetType, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByTenantIdAndSearchTextAndUsersFilterTypeIfPresent(tenantId, List.of(notificationTargetType.name()), pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}

	@Override
	public void removeByTenantId(String tenantId) {
		repository.deleteByTenantId(tenantId);
	}

	@Override
	public PageData<NotificationTarget> findNotificationTargetsByTenantId(String tenantId, NotificationType notificationType, PageLink pageLink) {
		return DaoUtil.toPageData(null);
	}

	@Override
	public Long countByTenantId(String tenantId) {
		return repository.countByTenantId(tenantId);
	}
}
