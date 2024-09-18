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
