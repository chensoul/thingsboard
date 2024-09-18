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
