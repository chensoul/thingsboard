package org.thingsboard.domain.oauth2.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.jpa.JpaAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.model.OAuth2Domain;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class OAuth2DomainJpaDao extends JpaAbstractDao<OAuth2DomainEntity, OAuth2Domain, Long> implements OAuth2DomainDao {
	private final OAuth2DomainRepository repository;

	@Override
	protected Class<OAuth2DomainEntity> getEntityClass() {
		return OAuth2DomainEntity.class;
	}

	@Override
	protected JpaRepository<OAuth2DomainEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public List<OAuth2Domain> findByOAuth2ParamId(Long oauth2ParamId) {
		return DaoUtil.convertDataList(repository.findByOauth2ParamId(oauth2ParamId));
	}
}
