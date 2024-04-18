package org.thingsboard.domain.setting.system.persistence;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.JpaAbstractDao;
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
public class SystemSettingJpaDao extends JpaAbstractDao<SystemSettingEntity, SystemSetting, Long> implements SystemSettingDao {
	private SystemSettingRepository repository;

	@Override
	protected Class<SystemSettingEntity> getEntityClass() {
		return SystemSettingEntity.class;
	}

	@Override
	protected JpaRepository<SystemSettingEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public SystemSetting findByType(String tenantId, SystemSettingType type) {
		return DaoUtil.getData(repository.findByTenantIdAndType(tenantId, type));
	}

	@Override
	public void removeByTenantIdAndType(String tenantId, SystemSettingType type) {
		repository.deleteByTenantIdAndType(tenantId, type);
	}

	@Override
	public void removeByTenantId(String tenantId) {
		repository.deleteByTenantId(tenantId);
	}
}
