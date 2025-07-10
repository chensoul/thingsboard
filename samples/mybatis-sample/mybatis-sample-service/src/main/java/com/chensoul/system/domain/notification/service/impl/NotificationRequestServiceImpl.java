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

import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.notification.controller.NotificationRequestInfo;
import com.chensoul.system.domain.notification.domain.NotificationRequest;
import com.chensoul.system.domain.notification.mybatis.NotificationRequestDao;
import com.chensoul.system.domain.notification.service.NotificationRequestService;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class NotificationRequestServiceImpl implements NotificationRequestService {
    private final NotificationRequestDao notificationRequestDao;

    @Override
    public NotificationRequest saveNotificationRequest(NotificationRequest notificationRequest) {
        return notificationRequestDao.save(notificationRequest);
    }

    @Override
    public NotificationRequest findNotificationRequestById(Long id) {
        return notificationRequestDao.findById(id);
    }

    @Override
    public NotificationRequestInfo findNotificationRequestInfoById(Long id) {
        NotificationRequest request = notificationRequestDao.findById(id);
        NotificationRequestInfo requestInfo = new NotificationRequestInfo();

        return requestInfo;
    }

    @Override
    public PageData<NotificationRequestInfo> findNotificationRequestsInfosByTenantIdAndOriginatorType(String tenantId, EntityType originatorType, PageLink pageLink) {
        return null;
    }

    @Override
    public List<NotificationRequest> findNotificationRequestsByRuleIdAndOriginatorEntityId(Long ruleId, Serializable originatorEntityId) {
        return Arrays.asList();
    }

    @Override
    public void deleteNotificationRequest(Long id) {
        notificationRequestDao.removeById(id);
    }
}
