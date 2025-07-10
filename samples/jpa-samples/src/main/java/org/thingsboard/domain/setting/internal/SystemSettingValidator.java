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
package org.thingsboard.domain.setting.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.data.service.DataValidator;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.internal.persistence.SystemSettingDao;

@Component
@RequiredArgsConstructor
public class SystemSettingValidator extends DataValidator<SystemSetting> {
	private final SystemSettingDao systemSettingDao;

	@Override
	protected void validateCreate(SystemSetting systemSetting) {
		SystemSetting existingSetting = systemSettingDao.findByType(systemSetting.getTenantId(), systemSetting.getType());
		if (existingSetting != null) {
			throw new DataValidationException("System setting with such name already exists!");
		}
	}

	@Override
	protected SystemSetting validateUpdate(SystemSetting systemSetting) {
		SystemSetting existentSystemSetting = systemSettingDao.findById(systemSetting.getId());
		if (existentSystemSetting != null) {
			if (!existentSystemSetting.getType().equals(systemSetting.getType())) {
				throw new DataValidationException("Changing key of system setting entry is prohibited!");
			}
		}
		return existentSystemSetting;
	}

	@Override
	protected void validateDataImpl(SystemSetting systemSetting) {
		if (systemSetting.getExtra() == null) {
			throw new DataValidationException("Json value should be specified!");
		}
	}
}
