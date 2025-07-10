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
package com.chensoul.system.domain.notificationrule.internal.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.notificationrule.NotificationRule;
import com.chensoul.system.domain.notificationrule.NotificationRuleTriggerType;
import java.util.Arrays;
import java.util.List;
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
public class NotificationRuleDao extends AbstractDao<NotificationRuleEntity, NotificationRule, Long> {

  private final NotificationRuleRepository notificationRuleRepository;

  @Override
  protected Class<NotificationRuleEntity> getEntityClass() {
    return NotificationRuleEntity.class;
  }

  @Override
  protected BaseMapper<NotificationRuleEntity> getRepository() {
    return notificationRuleRepository;
  }

  public List<NotificationRule> findByTenantIdAndTriggerTypeAndEnabled(String tenantId, NotificationRuleTriggerType triggerType, boolean enabled) {
    return Arrays.asList();
  }
}
