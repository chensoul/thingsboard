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
package com.chensoul.system.domain.setting.service;

import com.chensoul.system.infrastructure.security.mfa.config.MfaConfig;
import com.chensoul.system.infrastructure.security.mfa.config.UserMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.chensoul.system.domain.setting.domain.MfaConfigRequest;
import com.chensoul.system.domain.setting.domain.MfaSetting;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.User;
import java.util.Optional;

public interface MfaSettingService {

    void checkProvider(String tenantId, MfaProviderType providerType);

    void prepareVerificationCode(SecurityUser user, MfaConfig mfaConfig, boolean checkLimits);

    boolean checkVerificationCode(SecurityUser user, String verificationCode, MfaConfig mfaConfig, boolean checkLimits);

    MfaConfig generateUserMfaConfig(User user, MfaProviderType providerType);

    Optional<UserMfaConfig> getUserMfaConfig(Long userId);

    Optional<MfaConfig> getUserMfaConfig(Long userId, MfaProviderType providerType);

    UserMfaConfig saveUserMfaConfig(Long userId, MfaConfig mfaConfig);

    UserMfaConfig verifyAndSaveUserMfaConfig(SecurityUser user, MfaConfig mfaConfig, String verificationCode);

    UserMfaConfig updateUserMfaConfig(Long userId, MfaProviderType providerType, MfaConfigRequest updateRequest);

    UserMfaConfig deleteUserMfaConfig(Long userId, MfaProviderType providerType);

    Optional<MfaSetting> getSystemMfaSetting(boolean sysetmSettingAsDefault);

    MfaSetting saveSystemMfaSetting(String tenantId, MfaSetting mfaSetting);

    void deleteSystemMfaSetting(String tenantId);
}
