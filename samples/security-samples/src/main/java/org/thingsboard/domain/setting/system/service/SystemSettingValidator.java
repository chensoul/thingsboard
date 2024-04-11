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
package org.thingsboard.domain.setting.system.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.exception.DataValidationException;
import org.thingsboard.common.service.DataValidator;
import org.thingsboard.domain.setting.system.model.SystemSetting;

@Component
@AllArgsConstructor
public class SystemSettingValidator extends DataValidator<SystemSetting> {

	private final SystemSettingService systemSettingService;

	@Override
	protected void validateCreate(SystemSetting systemSetting) {
		SystemSetting existingSettings = systemSettingService.findSystemSettingByType(systemSetting.getTenantId(), systemSetting.getType());
		if (existingSettings != null) {
			throw new DataValidationException("Admin settings with such name already exists!");
		}

	}

	@Override
	protected SystemSetting validateUpdate(SystemSetting systemSetting) {
		SystemSetting existentSystemSetting = systemSettingService.findSystemSettingById(systemSetting.getId());
		if (existentSystemSetting != null) {
			if (!existentSystemSetting.getType().equals(systemSetting.getType())) {
				throw new DataValidationException("Changing key of admin settings entry is prohibited!");
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
