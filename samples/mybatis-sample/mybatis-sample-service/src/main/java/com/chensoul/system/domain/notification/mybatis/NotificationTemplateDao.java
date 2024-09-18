package com.chensoul.system.domain.notification.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.notification.domain.template.NotificationTemplate;
import com.chensoul.system.domain.notification.domain.template.NotificationType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class NotificationTemplateDao extends AbstractDao<NotificationTemplateEntity, NotificationTemplate, Long> {
  private final NotificationTemplateMapper repository;

  @Override
  protected Class<NotificationTemplateEntity> getEntityClass() {
    return NotificationTemplateEntity.class;
  }

  @Override
  protected BaseMapper<NotificationTemplateEntity> getRepository() {
    return repository;
  }

  public Page<NotificationTemplate> findByTenantIdAndTemplateTypes(Pageable pageable, String tenantId, List<NotificationType> templateTypes) {

    return DaoUtil.toPage(repository.findByTenantIdAndNotificationTypesAndSearchText(pageable, tenantId, templateTypes, null));
  }

  public void removeByTenantId(String tenantId) {
    repository.deleteByTenantId(tenantId);
  }
}
