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
