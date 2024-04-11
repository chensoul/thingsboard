/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.setting.system.mybatis;

import org.thingsboard.common.dao.Dao;
import org.thingsboard.domain.setting.system.model.SystemSetting;
import org.thingsboard.domain.setting.system.model.SystemSettingType;

public interface SystemSettingDao extends Dao<SystemSetting> {
	SystemSetting findByType(String tenantId, SystemSettingType type);

	boolean removeByTenantIdAndKey(String tenantId, SystemSettingType type);

	void removeByTenantId(String tenantId);
}
