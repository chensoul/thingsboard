package com.chensoul.system.domain.notification.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.notification.domain.NotificationRequest;
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
public class NotificationRequestDao extends AbstractDao<NotificationRequestEntity, NotificationRequest, Long> {
    private final NotificationRequestMapper repository;

    @Override
    protected Class<NotificationRequestEntity> getEntityClass() {
        return NotificationRequestEntity.class;
    }

    @Override
    protected BaseMapper<NotificationRequestEntity> getRepository() {
        return repository;
    }
}
