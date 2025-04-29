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
package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.OAuth2Param;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class OAuth2ParamJpaDao extends JpaAbstractDao<OAuth2ParamEntity, OAuth2Param, Long> implements OAuth2ParamDao {
	private final OAuth2ParamRepository repository;

	@Override
	protected Class<OAuth2ParamEntity> getEntityClass() {
		return OAuth2ParamEntity.class;
	}

	@Override
	protected JpaRepository<OAuth2ParamEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public List<OAuth2Param> findByTenantId(String tenantId) {
		return DaoUtil.convertDataList(repository.findByTenantId(tenantId));
	}
}
