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

import java.io.Serializable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.notification.NotificationRequest;

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
		return List.of();
	}

	@Override
	public void deleteNotificationRequest(Long id) {
		notificationRequestDao.removeById(id);
	}
}
