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
package org.thingsboard.domain.tenant.internal.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.tenant.TenantProfile;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class TenantProfileJpaDao extends JpaAbstractDao<TenantProfileEntity, TenantProfile, Long> implements TenantProfileDao {
	private final TenantProfileRepository repository;

	@Override
	protected Class<TenantProfileEntity> getEntityClass() {
		return TenantProfileEntity.class;
	}

	@Override
	protected JpaRepository<TenantProfileEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public TenantProfile findDefaultTenantProfile() {
		return DaoUtil.getData(repository.findByDefaultTrue());
	}

	@Override
	public PageData<TenantProfile> findTenantProfiles(PageLink pageLink) {
		return DaoUtil.toPageData(repository.findTenantProfiles(pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}
}
