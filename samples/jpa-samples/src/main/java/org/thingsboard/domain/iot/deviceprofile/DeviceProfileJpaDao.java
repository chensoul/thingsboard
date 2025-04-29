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
package org.thingsboard.domain.iot.deviceprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class DeviceProfileJpaDao extends JpaAbstractDao<DeviceProfileEntity, DeviceProfile, Long> implements DeviceProfileDao {
	private final DeviceProfileRepository repository;

	@Override
	protected Class<DeviceProfileEntity> getEntityClass() {
		return DeviceProfileEntity.class;
	}

	@Override
	protected JpaRepository<DeviceProfileEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public DeviceProfileInfo findDeviceProfileInfoById(Long deviceProfileId) {
		return new DeviceProfileInfo(DaoUtil.getData(repository.findById(deviceProfileId)));
	}

	@Override
	public DeviceProfile findDefaultDeviceProfile(String tenantId) {
		return null;
	}

	@Override
	public DeviceProfile findByName(String tenantId, String profileName) {
		return null;
	}
}
