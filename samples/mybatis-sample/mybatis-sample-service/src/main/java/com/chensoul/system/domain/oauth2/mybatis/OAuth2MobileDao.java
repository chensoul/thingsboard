package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.oauth2.domain.OAuth2Mobile;
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
public class OAuth2MobileDao extends AbstractDao<OAuth2MobileEntity, OAuth2Mobile, Long> {
  private final OAuth2MobileMapper repository;

  @Override
  protected Class<OAuth2MobileEntity> getEntityClass() {
    return OAuth2MobileEntity.class;
  }

  @Override
  protected BaseMapper<OAuth2MobileEntity> getRepository() {
    return repository;
  }

  public List<OAuth2Mobile> findByOAuth2ParamId(Long oauth2ParamId) {
    return DaoUtil.convertDataList(repository.findByOauth2ParamId(oauth2ParamId));
  }
}
