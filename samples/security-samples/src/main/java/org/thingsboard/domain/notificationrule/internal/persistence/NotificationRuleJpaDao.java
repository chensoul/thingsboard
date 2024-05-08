package org.thingsboard.domain.notificationrule.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.domain.notificationrule.NotificationRule;
import org.thingsboard.domain.notificationrule.NotificationRuleTriggerType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class NotificationRuleJpaDao extends JpaAbstractDao<NotificationRuleEntity, NotificationRule, Long> implements NotificationRuleDao {

	private final NotificationRuleRepository notificationRuleRepository;

	@Override
	protected Class<NotificationRuleEntity> getEntityClass() {
		return NotificationRuleEntity.class;
	}

	@Override
	protected JpaRepository<NotificationRuleEntity, Long> getRepository() {
		return notificationRuleRepository;
	}

	@Override
	public List<NotificationRule> findByTenantIdAndTriggerTypeAndEnabled(String tenantId, NotificationRuleTriggerType triggerType, boolean enabled) {
		return List.of();
	}
}
