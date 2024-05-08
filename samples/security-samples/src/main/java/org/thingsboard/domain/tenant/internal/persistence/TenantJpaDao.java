package org.thingsboard.domain.tenant.internal.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.domain.tenant.Tenant;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class TenantJpaDao extends JpaAbstractDao<TenantEntity, Tenant, String> implements TenantDao {
	private final TenantRepository repository;

	@Override
	protected Class<TenantEntity> getEntityClass() {
		return TenantEntity.class;
	}

	@Override
	protected JpaRepository<TenantEntity, String> getRepository() {
		return repository;
	}

	@Override
	public Page<Tenant> findTenants(Pageable pageable, String textSearch) {
		return DaoUtil.toPage(repository.findTenants(pageable, textSearch));
	}

	@Override
	public Tenant findByName(String name) {
		return DaoUtil.getData(repository.findByName(name));
	}
}
