package org.thingsboard.domain.setting.internal.persistence;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.SystemSettingType;

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
