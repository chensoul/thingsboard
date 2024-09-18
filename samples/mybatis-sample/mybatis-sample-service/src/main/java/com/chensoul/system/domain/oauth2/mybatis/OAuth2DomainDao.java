package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.oauth2.domain.OAuth2Domain;
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
public class OAuth2DomainDao extends AbstractDao<OAuth2DomainEntity, OAuth2Domain, Long> {
  private final OAuth2DomainMapper repository;

  @Override
  protected Class<OAuth2DomainEntity> getEntityClass() {
    return OAuth2DomainEntity.class;
  }

  @Override
  protected BaseMapper<OAuth2DomainEntity> getRepository() {
    return repository;
  }

  public List<OAuth2Domain> findByOAuth2ParamId(Long oauth2ParamId) {
    return DaoUtil.convertDataList(repository.findByOauth2ParamId(oauth2ParamId));
  }
}
