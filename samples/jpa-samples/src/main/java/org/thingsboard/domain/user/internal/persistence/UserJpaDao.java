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
package org.thingsboard.domain.user.internal.persistence;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.domain.user.Authority;
import org.thingsboard.domain.user.User;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class UserJpaDao extends JpaAbstractDao<UserEntity, User, Long> implements UserDao {
	private final UserRepository repository;

	@Override
	protected Class<UserEntity> getEntityClass() {
		return UserEntity.class;
	}

	@Override
	protected JpaRepository<UserEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public User findByEmail(String email) {
		return DaoUtil.getData(repository.findByEmail(email));
	}

	@Override
	public PageData<User> findByMerchantIds(Set<Long> merchantIds, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByMerchantIds(merchantIds, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}

	@Override
	public PageData<User> findByTenantId(String tenantId, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByTenantId(tenantId, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}

	@Override
	public PageData<User> findUsersByIds(Set<Long> ids, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByIds(ids, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}

	@Override
	public PageData<User> findUsers(PageLink pageLink) {
		return DaoUtil.toPageData(repository.find(pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}

	@Override
	public PageData<User> findByTenantIdsAndAuthority(Set<String> tenantIds, Authority authority, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByTenantIdsAndAuthority(tenantIds, authority, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}

	@Override
	public PageData<User> findByAuthority(Authority authority, PageLink pageLink) {
		return DaoUtil.toPageData(repository.findByAuthority(authority, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
	}

	@Override
	public Long countByTenantId(String tenantId) {
		return repository.countByTenantId(tenantId);
	}
}
