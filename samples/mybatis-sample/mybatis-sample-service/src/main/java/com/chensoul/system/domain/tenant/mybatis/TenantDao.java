package com.chensoul.system.domain.tenant.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chensoul.system.domain.tenant.domain.Tenant;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
//@SqlDao
@RequiredArgsConstructor
@Component
public class TenantDao extends AbstractDao<TenantEntity, Tenant, String> {
  private final TenantMapper repository;

  @Override
  protected Class<TenantEntity> getEntityClass() {
    return TenantEntity.class;
  }

  @Override
  protected BaseMapper<TenantEntity> getRepository() {
    return repository;
  }

  public Page<Tenant> findTenants(Pageable pageable, String textSearch) {
    return null;// DaoUtil.toPage(repository.selectPage(pageable, Wrappers.<TenantEntity>lambdaQuery().like(TenantEntity::getName, textSearch)));
  }

  public Tenant findByName(String name) {
    return DaoUtil.getData(repository.selectOne(Wrappers.<TenantEntity>lambdaQuery().eq(TenantEntity::getName, name)));
  }
}
