package com.chensoul.system.domain.notification.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.notification.domain.Notification;
import com.chensoul.system.domain.notification.domain.NotificationStatus;
import com.chensoul.system.domain.notification.domain.template.NotificationDeliveryMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
//@SqlDao
@RequiredArgsConstructor
@Component
public class NotificationDao extends AbstractDao<NotificationEntity, Notification, Long> {
    private final NotificationRepository repository;

    @Override
    protected Class<NotificationEntity> getEntityClass() {
        return NotificationEntity.class;
    }

    @Override
    protected BaseMapper<NotificationEntity> getRepository() {
        return repository;
    }


    public boolean updateStatusByIdAndRecipientId(Long recipientId, Long notificationId, NotificationStatus status) {
        return repository.updateStatusByIdAndRecipientId(notificationId, recipientId, status) != 0;
    }

    public boolean deleteByIdAndRecipientId(Long recipientId, Long notificationId) {
        return repository.deleteByIdAndRecipientId(notificationId, recipientId) != 0;
    }

    public int updateStatusByDeliveryMethodAndRecipientId(NotificationDeliveryMethod deliveryMethod, Long recipientId, NotificationStatus notificationStatus) {
        return updateStatusByDeliveryMethodAndRecipientId(deliveryMethod, recipientId, notificationStatus);
    }

    public PageData<Notification> findUnreadByDeliveryMethodAndRecipientId(NotificationDeliveryMethod deliveryMethod, Long recipientId, PageLink pageLink) {
        return DaoUtil.toPageData(repository.findByDeliveryMethodAndRecipientIdAndStatusNot(deliveryMethod, recipientId, NotificationStatus.READ, pageLink.getTextSearch(),
            DaoUtil.toPageable(pageLink)));
    }

    public PageData<Notification> findByDeliveryMethodAndRecipientId(NotificationDeliveryMethod deliveryMethod, Long recipientId, PageLink pageLink) {
        return DaoUtil.toPageData(repository.findByDeliveryMethodAndRecipientId(deliveryMethod, recipientId, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }
}
