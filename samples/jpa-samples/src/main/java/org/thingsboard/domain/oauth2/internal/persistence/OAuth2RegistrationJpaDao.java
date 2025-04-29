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
import org.thingsboard.domain.oauth2.OAuth2Registration;
import org.thingsboard.domain.oauth2.PlatformType;
import org.thingsboard.domain.oauth2.SchemeType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class OAuth2RegistrationJpaDao extends JpaAbstractDao<OAuth2RegistrationEntity, OAuth2Registration, String> implements OAuth2RegistrationDao {
	private final OAuth2RegistrationRepository repository;

	@Override
	protected Class<OAuth2RegistrationEntity> getEntityClass() {
		return OAuth2RegistrationEntity.class;
	}

	@Override
	protected JpaRepository<OAuth2RegistrationEntity, String> getRepository() {
		return repository;
	}

	@Override
	public List<OAuth2Registration> findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(List<SchemeType> domainSchemes, String domainName, String pkgName, PlatformType platformType) {
		return DaoUtil.convertDataList(repository.findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(domainSchemes, domainName, pkgName,
			platformType != null ? "%" + platformType.name() + "%" : null));
	}

	@Override
	public List<OAuth2Registration> findByOAuth2ParamId(Long oauth2ParamId) {
		return DaoUtil.convertDataList(repository.findByOauth2ParamId(oauth2ParamId));
	}

	@Override
	public String findAppSecret(String id, String pkgName) {
		return repository.findAppSecret(id, pkgName);
	}
}
