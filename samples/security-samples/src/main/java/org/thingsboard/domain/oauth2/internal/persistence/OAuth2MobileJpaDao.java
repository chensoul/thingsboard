package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.OAuth2Mobile;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class OAuth2MobileJpaDao extends JpaAbstractDao<OAuth2MobileEntity, OAuth2Mobile, Long> implements OAuth2MobileDao {
	private final OAuth2MobileRepository repository;

	@Override
	protected Class<OAuth2MobileEntity> getEntityClass() {
		return OAuth2MobileEntity.class;
	}

	@Override
	protected JpaRepository<OAuth2MobileEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public List<OAuth2Mobile> findByOAuth2ParamId(Long oauth2ParamId) {
		return DaoUtil.convertDataList(repository.findByOauth2ParamId(oauth2ParamId));
	}
}
