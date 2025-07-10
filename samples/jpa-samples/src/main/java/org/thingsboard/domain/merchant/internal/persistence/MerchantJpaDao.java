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
package org.thingsboard.domain.merchant.internal.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.merchant.Merchant;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MerchantJpaDao extends JpaAbstractDao<MerchantEntity, Merchant, Long> implements MerchantDao {
	private final MerchantRepository repository;

	@Override
	protected Class<MerchantEntity> getEntityClass() {
		return MerchantEntity.class;
	}

	@Override
	protected JpaRepository<MerchantEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String name) {
		MerchantEntity entity = repository.findByTenantIdAndName(tenantId, name);
		return Optional.of(DaoUtil.getData(entity));
	}

	@Override
	public void removeByTenantId(String tenantId) {
		repository.deleteByTenantId(tenantId);
	}

	@Override
	public PageData<Merchant> findTenants(String tenantId, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByTenantId(tenantId, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}
}
