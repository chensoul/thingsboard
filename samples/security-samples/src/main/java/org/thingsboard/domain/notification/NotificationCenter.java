package org.thingsboard.domain.notification;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Set;
import org.hibernate.annotations.TenantId;
import org.thingsboard.domain.setting.notification.NotificationSetting;
import org.thingsboard.domain.notification.template.NotificationDeliveryMethod;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface NotificationCenter {
	NotificationRequest processNotificationRequest(NotificationRequest request, FutureCallback<NotificationRequestStats> callback);

	Set<NotificationDeliveryMethod> getAvailableDeliveryTypes(String tenantId);

	void deleteNotificationRequest(Long notificationRequestId);


}
