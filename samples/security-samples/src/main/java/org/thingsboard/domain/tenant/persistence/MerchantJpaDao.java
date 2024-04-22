package org.thingsboard.domain.tenant.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.jpa.JpaAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.page.PageData;
import org.thingsboard.common.dao.page.PageLink;
import org.thingsboard.domain.tenant.model.Merchant;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MerchantJpaDao extends JpaAbstractDao<MerchantEntity, Merchant, Long> implements MerchantDao {
	private final MerchantRepository repository;

	@Override
	protected Class<MerchantEntity> getEntityClass() {
		return MerchantEntity.class;
	}

	@Override
	protected JpaRepository<MerchantEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String name) {
		MerchantEntity entity = repository.findByTenantIdAndName(tenantId, name);
		return Optional.of(DaoUtil.getData(entity));
	}

	@Override
	public void removeByTenantId(String tenantId) {
		repository.deleteByTenantId(tenantId);
	}

	@Override
	public PageData<Merchant> findTenants(String tenantId, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByTenantId(tenantId, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}
}
