package org.thingsboard.domain.setting.system.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
import org.thingsboard.domain.setting.system.SystemSetting;
import org.thingsboard.domain.setting.system.SystemSettingType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@AllArgsConstructor
@Component
public class MybatisSystemSettingDao extends AbstractMybatisDao<SystemSettingEntity, SystemSetting> implements SystemSettingDao {
	private SystemSettingMapper mapper;

	@Override
	protected Class<SystemSettingEntity> getEntityClass() {
		return SystemSettingEntity.class;
	}

	@Override
	protected BaseMapper<SystemSettingEntity> getRepository() {
		return mapper;
	}

	@Override
	public SystemSetting findByType(String tenantId, SystemSettingType type) {
		SystemSettingEntity entity = getRepository().selectOne(Wrappers.<SystemSettingEntity>lambdaQuery()
			.eq(SystemSettingEntity::getTenantId, tenantId).eq(SystemSettingEntity::getType, type.name()));
		return DaoUtil.getData(entity);
	}

	@Override
	public boolean removeByTenantIdAndKey(String tenantId, SystemSettingType type) {
		return getRepository().delete(Wrappers.<SystemSettingEntity>lambdaQuery()
			.eq(SystemSettingEntity::getTenantId, tenantId).eq(SystemSettingEntity::getType, type.name())) > 0;
	}

	@Override
	public void removeByTenantId(String tenantId) {
		getRepository().delete(Wrappers.<SystemSettingEntity>lambdaQuery()
			.eq(SystemSettingEntity::getTenantId, tenantId));
	}
}
