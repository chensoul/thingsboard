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
import org.thingsboard.domain.oauth2.OAuth2ClientRegistrationTemplate;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class OAuth2ClientRegistrationTemplateJpaDao extends JpaAbstractDao<OAuth2ClientRegistrationTemplateEntity, OAuth2ClientRegistrationTemplate, Long> implements OAuth2ClientRegistrationTemplateDao {
	private final OAuth2ClientRegistrationTemplateRepository repository;

	@Override
	protected Class<OAuth2ClientRegistrationTemplateEntity> getEntityClass() {
		return OAuth2ClientRegistrationTemplateEntity.class;
	}

	@Override
	protected JpaRepository<OAuth2ClientRegistrationTemplateEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public OAuth2ClientRegistrationTemplate findByProviderId(String providerId) {
		return DaoUtil.getData(repository.findByProviderId(providerId));
	}

	@Override
	public List<OAuth2ClientRegistrationTemplate> findAll() {
		return DaoUtil.convertDataList(repository.findAll());
	}
}
