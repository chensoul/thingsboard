package org.thingsboard.domain.notification;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.notification.info.AlarmNotificationInfo;
import org.thingsboard.domain.notification.info.NotificationInfo;
import org.thingsboard.domain.notification.settings.NotificationSettings;
import org.thingsboard.domain.notification.targets.NotificationTarget;
import org.thingsboard.domain.notification.targets.PlatformUserNotificationTargetConfig;
import org.thingsboard.domain.notification.template.NotificationTemplate;
import org.thingsboard.domain.notification.template.NotificationType;

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
		NotificationInfo notificationInfo = new AlarmNotificationInfo("1", "offline");

		NotificationRequest request = NotificationRequest.builder()
			.tenantId(tenantId)
//			.targets(targets)
			.template(notificationTemplate)
			.info(notificationInfo)
			.config(new NotificationRequestConfig())
			.build();

		NotificationSettings settings = new NotificationSettings();

		defaultNotificationCenter.processNotificationRequest(request, settings, null);
	}
}
