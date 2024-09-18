package com.chensoul.system.domain.notification.service;

import com.chensoul.system.domain.notification.domain.NotificationRequest;
import com.chensoul.system.domain.notification.domain.NotificationRequestStats;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import com.google.common.util.concurrent.FutureCallback;
import java.util.Set;

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
