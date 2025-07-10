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
package org.thingsboard.domain.setting;

import java.util.Optional;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.setting.internal.mfa.config.TwoFaConfig;
import org.thingsboard.domain.setting.internal.mfa.config.UserTwoFaSetting;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderType;
import org.thingsboard.domain.user.User;
import org.thingsboard.server.security.SecurityUser;

public interface TwoFaSettingService {

	void checkProvider(String tenantId, TwoFaProviderType providerType) throws ThingsboardException;

	void prepareVerificationCode(SecurityUser user, TwoFaConfig twoFaConfig, boolean checkLimits) throws ThingsboardException;

	boolean checkVerificationCode(SecurityUser user, String verificationCode, TwoFaConfig twoFaConfig, boolean checkLimits) throws ThingsboardException;

	TwoFaConfig generateUserTwoFaConfig(User user, TwoFaProviderType providerType) throws ThingsboardException;

	Optional<UserTwoFaSetting> getUserTwoFaSetting(Long userId);

	Optional<TwoFaConfig> getUserTwoFaConfig(Long userId, TwoFaProviderType providerType);

	UserTwoFaSetting saveUserTwoFaConfig(Long userId, TwoFaConfig twoFaConfig);

	UserTwoFaSetting verifyAndSaveUserTwoFaConfig(SecurityUser user, TwoFaConfig twoFaConfig, String verificationCode);

	UserTwoFaSetting updateUserTwoFaConfig(Long userId, TwoFaProviderType providerType, TwoFaConfigUpdateRequest updateRequest);

	UserTwoFaSetting deleteUserTwoFaConfig(Long userId, TwoFaProviderType providerType);

	Optional<TwoFaSystemSetting> getTwoFaSystemSetting(boolean sysadminSettingsAsDefault);

	TwoFaSystemSetting saveTwoFaSystemSetting(String tenantId, TwoFaSystemSetting twoFactorAuthSettings) throws ThingsboardException;

	void deleteTwoFaSystemSetting(String tenantId);
}
