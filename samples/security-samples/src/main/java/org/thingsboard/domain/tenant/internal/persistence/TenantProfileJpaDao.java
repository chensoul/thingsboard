package org.thingsboard.domain.tenant.internal.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.tenant.TenantProfile;

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
