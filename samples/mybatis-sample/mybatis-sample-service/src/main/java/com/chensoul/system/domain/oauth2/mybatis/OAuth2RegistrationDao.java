package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.oauth2.domain.OAuth2Registration;
import com.chensoul.system.domain.oauth2.domain.PlatformType;
import com.chensoul.system.domain.oauth2.domain.SchemeType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Component
@RequiredArgsConstructor
public class OAuth2RegistrationDao extends AbstractDao<OAuth2RegistrationEntity, OAuth2Registration, String> {
  private final OAuth2RegistrationMapper repository;

  @Override
  protected Class<OAuth2RegistrationEntity> getEntityClass() {
    return OAuth2RegistrationEntity.class;
  }

  @Override
  protected BaseMapper<OAuth2RegistrationEntity> getRepository() {
    return repository;
  }

  public List<OAuth2Registration> findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(List<SchemeType> domainSchemes, String domainName, String pkgName, PlatformType platformType) {
    return DaoUtil.convertDataList(repository.findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(domainSchemes, domainName, pkgName,
            platformType != null ? "%" + platformType.name() + "%" : null));
  }

  public List<OAuth2Registration> findByOAuth2ParamId(Long oauth2ParamId) {
    return DaoUtil.convertDataList(repository.findByOauth2ParamId(oauth2ParamId));
  }

  public String findAppSecret(String id, String pkgName) {
    return repository.findAppSecret(id, pkgName);
  }
}
