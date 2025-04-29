/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
