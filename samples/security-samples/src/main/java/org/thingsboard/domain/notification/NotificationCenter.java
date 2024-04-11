package org.thingsboard.domain.notification;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Set;
import org.thingsboard.domain.notification.settings.NotificationSettings;
import org.thingsboard.domain.notification.template.NotificationDeliveryType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface NotificationCenter {
	NotificationRequest processNotificationRequest(NotificationRequest request, NotificationSettings settings, FutureCallback<NotificationRequestStats> callback);

	Set<NotificationDeliveryType> getAvailableDeliveryTypes(String tenantId);

}
