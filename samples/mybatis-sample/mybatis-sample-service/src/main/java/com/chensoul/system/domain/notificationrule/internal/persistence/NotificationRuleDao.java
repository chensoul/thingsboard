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
