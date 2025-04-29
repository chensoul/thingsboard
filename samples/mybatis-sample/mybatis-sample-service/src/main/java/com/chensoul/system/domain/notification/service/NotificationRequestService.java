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
package com.chensoul.system.domain.notification.service;

import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.notification.controller.NotificationRequestInfo;
import com.chensoul.system.domain.notification.domain.NotificationRequest;
import java.io.Serializable;
import java.util.List;

public interface NotificationRequestService {

    NotificationRequest saveNotificationRequest(NotificationRequest notificationRequest);

    NotificationRequest findNotificationRequestById(Long id);

    NotificationRequestInfo findNotificationRequestInfoById(Long id);

    //	PageData<NotificationRequest> findNotificationRequestsByTenantIdAndOriginatorType(TenantId tenantId, EntityType originatorType, PageLink pageLink);
//
    PageData<NotificationRequestInfo> findNotificationRequestsInfosByTenantIdAndOriginatorType(String tenantId, EntityType originatorType, PageLink pageLink);

    //
//	List<NotificationRequestId> findNotificationRequestsIdsByStatusAndRuleId(TenantId tenantId, NotificationRequestStatus requestStatus, NotificationRuleId ruleId);
//
    List<NotificationRequest> findNotificationRequestsByRuleIdAndOriginatorEntityId(Long ruleId, Serializable originatorEntityId);

    //
    void deleteNotificationRequest(Long id);
//
//	PageData<NotificationRequest> findScheduledNotificationRequests(PageLink pageLink);
//
//	void updateNotificationRequest(TenantId tenantId, NotificationRequestId requestId, NotificationRequestStatus requestStatus, NotificationRequestStats stats);
//
//	void deleteNotificationRequestsByTenantId(TenantId tenantId);

}
