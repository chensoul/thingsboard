package com.chensoul.system.domain.oauth2.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.oauth2.domain.OAuth2Param;
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
public class OAuth2ParamDao extends AbstractDao<OAuth2ParamEntity, OAuth2Param, Long> {
  private final OAuth2ParamMapper repository;

  @Override
  protected Class<OAuth2ParamEntity> getEntityClass() {
    return OAuth2ParamEntity.class;
  }

  @Override
  protected BaseMapper<OAuth2ParamEntity> getRepository() {
    return repository;
  }

  public void deleteAll() {
    repository.delete(Wrappers.lambdaQuery());
  }

  public List<OAuth2Param> findByTenantId(String tenantId) {
    return DaoUtil.convertDataList(repository.findByTenantId(tenantId));
  }
}
