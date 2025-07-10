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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.domain.notification.internal.channel.mail.MailService;
import org.thingsboard.domain.notification.internal.channel.sms.SmsService;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.SystemSettingService;
import org.thingsboard.domain.setting.SystemSettingType;
import org.thingsboard.domain.setting.internal.persistence.SystemSettingDao;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class SystemSettingServiceImpl implements SystemSettingService {
	private final SystemSettingDao systemSettingDao;
	private final SystemSettingValidator systemSettingValidator;
	private final SmsService smsService;
	private final MailService mailService;

	@Override
	public SystemSetting findSystemSettingById(Long id) {
		return systemSettingDao.findById(id);
	}

	@Override
	public SystemSetting findSystemSettingByType(String tenantId, SystemSettingType type) {
		return systemSettingDao.findByType(tenantId, type);
	}

	@Override
	public SystemSetting saveSystemSetting(String tenantId, SystemSetting systemSetting) {
		systemSettingValidator.validate(systemSetting);

		if (systemSetting.getType().equals(SystemSettingType.EMAIL)) {
			SystemSetting mailSetting = findSystemSettingByType(tenantId, SystemSettingType.EMAIL);
			if (mailSetting != null) {
				JsonNode newJsonValue = systemSetting.getExtra();
				JsonNode oldJsonValue = mailSetting.getExtra();
				if (!newJsonValue.has("password") && oldJsonValue.has("password")) {
					((ObjectNode) newJsonValue).put("password", oldJsonValue.get("password").asText());
				}
				if (!newJsonValue.has("refreshToken") && oldJsonValue.has("refreshToken")) {
					((ObjectNode) newJsonValue).put("refreshToken", oldJsonValue.get("refreshToken").asText());
				}
				dropTokenIfProviderInfoChanged(newJsonValue, oldJsonValue);
			}
		}
		if (systemSetting.getTenantId() == null) {
			systemSetting.setTenantId(SYS_TENANT_ID);
		}
		SystemSetting newSystemSetting = systemSettingDao.save(systemSetting);

		if (systemSetting.getType().equals(SystemSettingType.EMAIL)) {
			mailService.updateMailConfiguration();
			((ObjectNode) systemSetting.getExtra()).remove("password");
			((ObjectNode) systemSetting.getExtra()).remove("refreshToken");
		} else if (systemSetting.getType().equals(SystemSettingType.SMS)) {
			smsService.updateSmsConfiguration();
		}
		return newSystemSetting;
	}

	@Override
	public void deleteSystemSettingByTenantIdAndType(String tenantId, SystemSettingType type) {
		systemSettingDao.removeByTenantIdAndType(tenantId, type);
	}

	@Override
	public void deleteSystemSettingByTenantId(String tenantId) {
		systemSettingDao.removeByTenantId(tenantId);
	}

	@Override
	public void deleteSystemSettingById(Long id) {
		systemSettingDao.removeById(id);
	}

	private void dropTokenIfProviderInfoChanged(JsonNode newJsonValue, JsonNode oldJsonValue) {
		if (newJsonValue.has("enableOauth2") && newJsonValue.get("enableOauth2").asBoolean()) {
			if (!newJsonValue.get("providerId").equals(oldJsonValue.get("providerId")) ||
				!newJsonValue.get("clientId").equals(oldJsonValue.get("clientId")) ||
				!newJsonValue.get("clientSecret").equals(oldJsonValue.get("clientSecret")) ||
				!newJsonValue.get("redirectUri").equals(oldJsonValue.get("redirectUri")) ||
				(newJsonValue.has("providerTenantId") && !newJsonValue.get("providerTenantId").equals(oldJsonValue.get("providerTenantId")))) {
				((ObjectNode) newJsonValue).put("tokenGenerated", false);
				((ObjectNode) newJsonValue).remove("refreshToken");
				((ObjectNode) newJsonValue).remove("refreshTokenExpires");
			}
		}
	}
}
