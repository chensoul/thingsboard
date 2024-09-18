package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.OAuth2Registration;
import org.thingsboard.domain.oauth2.PlatformType;
import org.thingsboard.domain.oauth2.SchemeType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class OAuth2RegistrationJpaDao extends JpaAbstractDao<OAuth2RegistrationEntity, OAuth2Registration, String> implements OAuth2RegistrationDao {
	private final OAuth2RegistrationRepository repository;

	@Override
	protected Class<OAuth2RegistrationEntity> getEntityClass() {
		return OAuth2RegistrationEntity.class;
	}

	@Override
	protected JpaRepository<OAuth2RegistrationEntity, String> getRepository() {
		return repository;
	}

	@Override
	public List<OAuth2Registration> findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(List<SchemeType> domainSchemes, String domainName, String pkgName, PlatformType platformType) {
		return DaoUtil.convertDataList(repository.findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(domainSchemes, domainName, pkgName,
			platformType != null ? "%" + platformType.name() + "%" : null));
	}

	@Override
	public List<OAuth2Registration> findByOAuth2ParamId(Long oauth2ParamId) {
		return DaoUtil.convertDataList(repository.findByOauth2ParamId(oauth2ParamId));
	}

	@Override
	public String findAppSecret(String id, String pkgName) {
		return repository.findAppSecret(id, pkgName);
	}
}
