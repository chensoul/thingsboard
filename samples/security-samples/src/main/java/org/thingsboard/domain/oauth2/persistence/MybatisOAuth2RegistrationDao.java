package org.thingsboard.domain.oauth2.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.model.OAuth2Registration;
import org.thingsboard.domain.oauth2.model.PlatformType;
import org.thingsboard.domain.oauth2.model.SchemeType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class MybatisOAuth2RegistrationDao extends MybatisAbstractDao<OAuth2RegistrationEntity, OAuth2Registration> implements OAuth2RegistrationDao {
	private final OAuth2RegistrationMapper mapper;

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
		return DaoUtil.convertDataList(mapper.findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(domainSchemes, domainName, pkgName,
			platformType != null ? "%" + platformType.name() + "%" : null));
	}

	@Override
	public List<OAuth2Registration> findByOAuth2ParamId(Long oauth2ParamId) {
		return DaoUtil.convertDataList(mapper.selectList(Wrappers.<OAuth2RegistrationEntity>lambdaQuery().eq(OAuth2RegistrationEntity::getOauth2ParamId, oauth2ParamId)));
	}

	@Override
	public String findAppSecret(String id, String pkgName) {
		return "";
	}
}
