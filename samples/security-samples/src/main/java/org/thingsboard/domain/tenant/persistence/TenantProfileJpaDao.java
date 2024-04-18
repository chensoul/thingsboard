package org.thingsboard.domain.tenant.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.JpaAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.jpa.PageData;
import org.thingsboard.common.dao.jpa.PageLink;
import org.thingsboard.domain.tenant.model.TenantProfile;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class TenantProfileJpaDao extends JpaAbstractDao<TenantProfileEntity, TenantProfile, Long> implements TenantProfileDao {
	private final TenantProfileRepository repository;

	@Override
	protected Class<TenantProfileEntity> getEntityClass() {
		return TenantProfileEntity.class;
	}

	@Override
	protected JpaRepository<TenantProfileEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public TenantProfile findDefaultTenantProfile() {
		return DaoUtil.getData(repository.findByDefaultTrue());
	}

	@Override
	public PageData<TenantProfile> findTenantProfiles(PageLink pageLink) {
		return DaoUtil.toPageData(repository.findTenantProfiles(pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}
}
