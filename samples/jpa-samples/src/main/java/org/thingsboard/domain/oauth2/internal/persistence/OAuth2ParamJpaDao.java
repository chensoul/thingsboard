package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.OAuth2Param;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class OAuth2ParamJpaDao extends JpaAbstractDao<OAuth2ParamEntity, OAuth2Param, Long> implements OAuth2ParamDao {
	private final OAuth2ParamRepository repository;

	@Override
	protected Class<OAuth2ParamEntity> getEntityClass() {
		return OAuth2ParamEntity.class;
	}

	@Override
	protected JpaRepository<OAuth2ParamEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public List<OAuth2Param> findByTenantId(String tenantId) {
		return DaoUtil.convertDataList(repository.findByTenantId(tenantId));
	}
}
