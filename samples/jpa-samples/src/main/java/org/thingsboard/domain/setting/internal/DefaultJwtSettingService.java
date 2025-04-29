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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.setting.JwtSetting;
import org.thingsboard.domain.setting.JwtSettingService;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.SystemSettingType;
import org.thingsboard.domain.setting.SystemSettingService;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultJwtSettingService implements JwtSettingService {
	private final SystemSettingService systemSettingService;
	private final JwtSettingValidator jwtSettingValidator;

	public static final String TOKEN_SIGNING_KEY_DEFAULT = "defaultSigningKey";
	private static final Integer tokenExpirationTime = 9000;
	private static final Integer refreshTokenExpTime = 604800;
	private static final String tokenIssuer = "thingsboard.io";
	private static final String tokenSigningKey = TOKEN_SIGNING_KEY_DEFAULT;

	private volatile JwtSetting jwtSetting = null; //lazy init

	/**
	 * Create JWT admin settings is intended to be called from Install scripts only
	 */
	@Override
	public void createRandomJwtSetting() {
		if (getJwtSettingsFromDb() == null) {
			log.info("Creating JWT setting...");
			this.jwtSetting = getDefaultJwtSetting();
			if (isSigningKeyDefault(jwtSetting)) {
				this.jwtSetting.setTokenSigningKey(Base64.getEncoder().encodeToString(
					RandomStringUtils.randomAlphanumeric(64).getBytes(StandardCharsets.UTF_8)));
			}
			saveJwtSetting(jwtSetting);
		} else {
			log.info("Skip creating JWT admin settings because they already exist.");
		}
	}

	@Override
	public JwtSetting saveJwtSetting(JwtSetting jwtSetting) {
		jwtSettingValidator.validate(jwtSetting);
		final SystemSetting adminJwtSettings = mapJwtToAdminSetting(jwtSetting);
		final SystemSetting existedSettings = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.JWT);
		if (existedSettings != null) {
			adminJwtSettings.setId(existedSettings.getId());
		}

		log.info("Saving new JWT setting. From this moment, the JWT parameters from YAML and ENV will be ignored");
		systemSettingService.saveSystemSetting(SYS_TENANT_ID, adminJwtSettings);

//		tbClusterService.ifPresent(cs -> cs.broadcastEntityStateChangeEvent(TenantId.SYS_TENANT_ID, TenantId.SYS_TENANT_ID, ComponentLifecycleEvent.UPDATED));
		return reloadJwtSetting();
	}

	@Override
	public JwtSetting reloadJwtSetting() {
		return getJwtSetting(true);
	}

	public JwtSetting getJwtSetting() {
		return getJwtSetting(false);
	}

	public JwtSetting getJwtSetting(boolean forceReload) {
		if (this.jwtSetting == null || forceReload) {
			synchronized (this) {
				if (this.jwtSetting == null || forceReload) {
					JwtSetting result = getJwtSettingsFromDb();
					if (result == null) {
						result = getDefaultJwtSetting();
						log.warn("Loading the JWT settings from default config since there are no settings in DB.");
					}
					if (isSigningKeyDefault(result)) {
						log.warn("WARNING: The platform is configured to use default JWT Signing Key. " +
								 "This is a security issue that needs to be resolved. Please change the JWT Signing Key using the Web UI. " +
								 "Navigate to \"System settings -> Security settings\" while logged in as a System Administrator.");
//						notificationCenter.ifPresent(notificationCenter -> {
//							notificationCenter.sendGeneralWebNotification(TenantId.SYS_TENANT_ID, new SystemAdministratorsFilter(), DefaultNotifications.jwtSigningKeyIssue.toTemplate());
//						});
					}
					this.jwtSetting = result;
				}
			}
		}
		return this.jwtSetting;
	}

	private JwtSetting getDefaultJwtSetting() {
		return new JwtSetting(this.tokenExpirationTime, this.refreshTokenExpTime, this.tokenIssuer, this.tokenSigningKey);
	}

	private JwtSetting getJwtSettingsFromDb() {
		SystemSetting adminJwtSettings = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.JWT);
		return adminJwtSettings != null ? mapAdminToJwtSetting(adminJwtSettings) : null;
	}

	private JwtSetting mapAdminToJwtSetting(SystemSetting systemSetting) {
		Objects.requireNonNull(systemSetting, "adminSettings for JWT is null");
		return JacksonUtil.treeToValue(systemSetting.getExtra(), JwtSetting.class);
	}

	private SystemSetting mapJwtToAdminSetting(JwtSetting jwtSetting) {
		Objects.requireNonNull(jwtSetting, "jwtSettings is null");
		SystemSetting adminJwtSettings = new SystemSetting();
		adminJwtSettings.setTenantId(SYS_TENANT_ID);
		adminJwtSettings.setType(SystemSettingType.JWT);
		adminJwtSettings.setExtra(JacksonUtil.valueToTree(jwtSetting));
		return adminJwtSettings;
	}

	private boolean isSigningKeyDefault(JwtSetting settings) {
		return TOKEN_SIGNING_KEY_DEFAULT.equals(settings.getTokenSigningKey());
	}

}
