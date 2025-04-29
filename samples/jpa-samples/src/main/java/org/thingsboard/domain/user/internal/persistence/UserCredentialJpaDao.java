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

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.domain.user.UserCredential;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
@SqlDao
public class UserCredentialJpaDao extends JpaAbstractDao<UserCredentialEntity, UserCredential, Long> implements UserCredentialDao {
	private final UserCredentialRepository repository;

	@Override
	protected Class<UserCredentialEntity> getEntityClass() {
		return UserCredentialEntity.class;
	}

	@Override
	protected JpaRepository<UserCredentialEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public UserCredential findByUserId(Long userId) {
		return DaoUtil.getData(repository.findByUserId(userId));
	}

	@Override
	public UserCredential findByActivateToken(String activateToken) {
		return DaoUtil.getData(repository.findByActivateToken(activateToken));
	}

	@Override
	public UserCredential findByResetToken(String resetToken) {
		return DaoUtil.getData(repository.findByResetToken(resetToken));
	}

	@Override
	@Transactional
	public void removeByUserId(Long userId) {
		repository.deleteByUserId(userId);
	}
}
