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
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.domain.user.UserSetting;
import org.thingsboard.domain.user.UserSettingType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
@SqlDao
public class UserSettingJpaDao extends JpaAbstractDao<UserSettingEntity, UserSetting, Long> implements UserSettingDao {
	private final UserSettingRepository repository;

	@Override
	protected Class<UserSettingEntity> getEntityClass() {
		return UserSettingEntity.class;
	}

	@Override
	protected JpaRepository<UserSettingEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public UserSetting findByUserIdAndType(Long userId, UserSettingType type) {
		UserSettingEntity entity = repository.findByUserIdAndType(userId, type);
		return DaoUtil.getData(entity);
	}

	@Override
	public void removeByUserId(Long userId) {
		repository.deleteByUserId(userId);
	}
}
