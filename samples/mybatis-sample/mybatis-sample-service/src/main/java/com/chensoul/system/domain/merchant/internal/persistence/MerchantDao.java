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
package com.chensoul.system.domain.merchant.internal.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.merchant.Merchant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class MerchantDao extends AbstractDao<MerchantEntity, Merchant, Long> {
  private final MerchantRepository repository;

  @Override
  protected Class<MerchantEntity> getEntityClass() {
    return MerchantEntity.class;
  }

  @Override
  protected BaseMapper<MerchantEntity> getRepository() {
    return repository;
  }

  public Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String name) {
    MerchantEntity entity = repository.findByTenantIdAndName(tenantId, name);
    return Optional.of(DaoUtil.getData(entity));
  }

  public void removeByTenantId(String tenantId) {
    repository.deleteByTenantId(tenantId);
  }

  public PageData<Merchant> findTenants(String tenantId, PageLink pageLink) {
    return DaoUtil.toPageData(repository.findByTenantId(tenantId, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
  }
}
