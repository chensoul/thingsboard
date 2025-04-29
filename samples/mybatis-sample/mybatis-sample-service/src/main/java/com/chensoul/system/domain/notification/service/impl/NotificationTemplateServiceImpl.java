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
package com.chensoul.system.domain.notification.service.impl;

import com.chensoul.system.domain.notification.domain.template.NotificationTemplate;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import com.chensoul.system.domain.notification.mybatis.NotificationTemplateDao;
import com.chensoul.system.domain.notification.service.NotificationTemplateService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    private final NotificationTemplateDao notificationTemplateDao;

    @Override
    public NotificationTemplate findNotificationTemplateById(Long id) {
        return notificationTemplateDao.findById(id);
    }

    @Override
    public NotificationTemplate saveNotificationTemplate(NotificationTemplate notificationTemplate) {
        return notificationTemplateDao.save(notificationTemplate);
    }

    @Override
    public Page<NotificationTemplate> findNotificationTemplatesByTenantIdAndTemplateTypes(Pageable pageable, String tenantId, List<NotificationType> notificationTypes) {
        return notificationTemplateDao.findByTenantIdAndTemplateTypes(pageable, tenantId, notificationTypes);
    }

    @Override
    public void deleteNotificationTemplateById(Long id) {
        notificationTemplateDao.removeById(id);
    }

    @Override
    public void deleteNotificationTemplatesByTenantId(String tenantId) {
        notificationTemplateDao.removeByTenantId(tenantId);
    }
}
