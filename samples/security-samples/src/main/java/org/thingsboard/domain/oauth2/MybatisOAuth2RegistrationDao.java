package org.thingsboard.domain.oauth2;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.MybatisAbstractDao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Component
@RequiredArgsConstructor
public class MybatisOAuth2RegistrationDao extends MybatisAbstractDao<OAuth2RegistrationEntity, OAuth2Registration> implements OAuth2RegistrationDao {
	private OAuth2RegistrationMapper mapper;

	@Override
	protected Class<OAuth2RegistrationEntity> getEntityClass() {
		return OAuth2RegistrationEntity.class;
	}

	@Override
	protected BaseMapper<OAuth2RegistrationEntity> getRepository() {
		return mapper;
	}

	@Override
	public List<OAuth2Registration> findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(List<SchemeType> domainSchemes, String domainName, String pkgName, PlatformType platformType) {
		return List.of();
	}

	@Override
	public List<OAuth2Registration> findByOAuth2ParamId(Long oauth2ParamId) {
		return List.of();
	}

	@Override
	public List<OAuth2Registration> findByTenantId(String tenantId) {
		return List.of();
	}

	@Override
	public String findAppSecret(String id, String pkgName) {
		return "";
	}
}
