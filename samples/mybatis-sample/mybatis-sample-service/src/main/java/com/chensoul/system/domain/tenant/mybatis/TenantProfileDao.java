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
import com.chensoul.system.domain.tenant.domain.TenantProfile;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.mybatis.dao.AbstractDao;
import lombok.RequiredArgsConstructor;
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
public class TenantProfileDao extends AbstractDao<TenantProfileEntity, TenantProfile, Long> {
  private final TenantProfileMapper mapper;

  @Override
  protected Class<TenantProfileEntity> getEntityClass() {
    return TenantProfileEntity.class;
  }

  @Override
  protected BaseMapper<TenantProfileEntity> getRepository() {
    return mapper;
  }

  public TenantProfile findDefaultTenantProfile() {
    return DaoUtil.getData(mapper.selectOne(Wrappers.<TenantProfileEntity>lambdaQuery().eq(TenantProfileEntity::isDefaulted, true)));
  }

  public PageData<TenantProfile> findTenantProfiles(PageLink pageLink) {
    return null;//DaoUtil.toPageData(mapper.findTenantProfiles(pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
  }
}
