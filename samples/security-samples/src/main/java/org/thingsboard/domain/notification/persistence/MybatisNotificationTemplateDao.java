package org.thingsboard.domain.notification.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.domain.notification.template.NotificationTemplate;
import org.thingsboard.domain.notification.template.NotificationType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class MybatisNotificationTemplateDao extends MybatisAbstractDao<NotificationTemplateEntity, NotificationTemplate> implements NotificationTemplateDao {
	private final NotificationTemplateMapper mapper;

	@Override
	protected Class<NotificationTemplateEntity> getEntityClass() {
		return NotificationTemplateEntity.class;
	}

	@Override
	protected BaseMapper<NotificationTemplateEntity> getRepository() {
		return mapper;
	}

	@Override
	public Page<NotificationTemplate> findByTenantIdAndTemplateTypes(Pageable pageable, String tenantId, List<NotificationType> templateTypes) {
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<NotificationTemplateEntity> selectPage = mapper.selectPage(toMybatisPage(pageable), Wrappers.<NotificationTemplateEntity>lambdaQuery().eq(NotificationTemplateEntity::getTenantId, tenantId)
			.in(CollectionUtils.isNotEmpty(templateTypes), NotificationTemplateEntity::getType, templateTypes));
		return DaoUtil.toPageData(selectPage);
	}

	@Override
	public void removeByTenantId(String tenantId) {
		mapper.delete(Wrappers.<NotificationTemplateEntity>lambdaQuery().eq(NotificationTemplateEntity::getTenantId, tenantId));
	}
}
