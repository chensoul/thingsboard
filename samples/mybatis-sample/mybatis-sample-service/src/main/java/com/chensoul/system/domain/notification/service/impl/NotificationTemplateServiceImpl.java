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
