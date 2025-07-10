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
package com.chensoul.system.domain.notification.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.notification.domain.targets.NotificationTarget;
import com.chensoul.system.domain.notification.domain.targets.NotificationTargetType;
import com.chensoul.system.domain.notification.domain.targets.UserFilterType;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
//@SqlDao
@AllArgsConstructor
@Component
public class NotificationTargetDao extends AbstractDao<NotificationTargetEntity, NotificationTarget, Long> {
    private NotificationTargetMapper repository;

    @Override
    protected Class<NotificationTargetEntity> getEntityClass() {
        return NotificationTargetEntity.class;
    }

    @Override
    protected BaseMapper<NotificationTargetEntity> getRepository() {
        return repository;
    }

    public List<NotificationTarget> findByTenantIdAndIds(String tenantId, Set<Long> ids) {
        return DaoUtil.convertDataList(repository.findByTenantIdAndIdIn(tenantId, ids));
    }

    public List<NotificationTarget> findByTenantIdAndUserFilterType(String tenantId, UserFilterType filterType) {
        return Arrays.asList();
    }

    public PageData<NotificationTarget> findByTenantIdAndSupportedNotificationType(String tenantId, NotificationTargetType notificationTargetType, PageLink pageLink) {
        return DaoUtil.toPageData(repository.findByTenantIdAndSearchTextAndUsersFilterTypeIfPresent(tenantId, Arrays.asList(notificationTargetType.name()), pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }

    public void removeByTenantId(String tenantId) {
        repository.deleteByTenantId(tenantId);
    }

    public PageData<NotificationTarget> findNotificationTargetsByTenantId(String tenantId, NotificationType notificationType, PageLink pageLink) {
        return DaoUtil.toPageData(null);
    }

    public Long countByTenantId(String tenantId) {
        return repository.countByTenantId(tenantId);
    }
}
