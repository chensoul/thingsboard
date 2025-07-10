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
