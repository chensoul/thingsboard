package org.thingsboard.domain.setting.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.common.service.DataValidator;
import org.thingsboard.domain.setting.system.model.SystemSetting;
import org.thingsboard.domain.setting.system.model.SystemSettingType;
import org.thingsboard.domain.setting.system.mybatis.SystemSettingDao;
import org.thingsboard.domain.setting.system.service.SystemSettingService;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
public class SystemSettingServiceImpl implements SystemSettingService {
	@Autowired
	private SystemSettingDao systemSettingDao;
	@Autowired
	private DataValidator<SystemSetting> adminSettingsValidator;

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
		adminSettingsValidator.validate(systemSetting);

		if (systemSetting.getType().equals(SystemSettingType.MAIL)) {
			SystemSetting mailSettings = findSystemSettingByType(tenantId, SystemSettingType.MAIL);
			if (mailSettings != null) {
				JsonNode newJsonValue = systemSetting.getExtra();
				JsonNode oldJsonValue = mailSettings.getExtra();
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
		return systemSettingDao.save(systemSetting);
	}

	@Override
	public boolean deleteSystemSettingByTenantIdAndType(String tenantId, SystemSettingType type) {
		return systemSettingDao.removeByTenantIdAndKey(tenantId, type);
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
