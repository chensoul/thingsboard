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
package org.thingsboard.domain.notification;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.notification.internal.info.AlarmNotificationInfo;
import org.thingsboard.domain.notification.internal.DefaultNotificationCenter;
import org.thingsboard.domain.notification.internal.targets.NotificationTarget;
import org.thingsboard.domain.notification.internal.targets.PlatformUserNotificationTargetConfig;
import org.thingsboard.domain.notification.internal.template.NotificationTemplate;
import org.thingsboard.domain.notification.internal.template.NotificationType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultNotificationCenterTest {
	@Autowired
	private DefaultNotificationCenter defaultNotificationCenter;

	@Test
	void processNotificationRequest() {
		String tenantId = "ROOT";
		NotificationTemplate notificationTemplate = new NotificationTemplate();
		notificationTemplate.setTenantId(tenantId);
		notificationTemplate.setName("New alarm notification");
		notificationTemplate.setType(NotificationType.ALARM);
//		notificationTemplate.setConfig();

		NotificationTarget notificationTarget = new NotificationTarget();
		notificationTarget.setName("test");
		String c1 = "{\"type\":\"PLATFORM_USERS\",\"description\":\"All users in scope of the tenant\",\"usersFilter\":{\"type\":\"ALL_USERS\"}}";
		notificationTarget.setConfig(JacksonUtil.fromString(c1, PlatformUserNotificationTargetConfig.class));
		notificationTarget.setTenantId(tenantId);

		List<NotificationTarget> targets = List.of(notificationTarget);
		NotificationInfo notificationInfo = new AlarmNotificationInfo();

		NotificationRequest request = NotificationRequest.builder()
			.tenantId(tenantId)
//			.targets(targets)
			.template(notificationTemplate)
			.info(notificationInfo)
			.config(new NotificationRequestConfig())
			.build();

		defaultNotificationCenter.processNotificationRequest(request, null);
	}
}
