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
package com.chensoul.system.domain.setting.service.impl;

import com.chensoul.exception.BusinessException;
import com.chensoul.json.JacksonUtils;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.setting.domain.MfaConfigRequest;
import com.chensoul.system.domain.setting.domain.MfaSetting;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import static com.chensoul.system.domain.setting.domain.SystemSettingType.MFA;
import com.chensoul.system.domain.setting.service.MfaSettingService;
import com.chensoul.system.domain.setting.service.SecuritySettingService;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.domain.usage.limit.LimitedApi;
import com.chensoul.system.domain.usage.limit.RateLimitService;
import com.chensoul.system.domain.user.mybatis.UserSettingDao;
import com.chensoul.system.domain.user.service.UserService;
import com.chensoul.system.infrastructure.security.mfa.config.MfaConfig;
import com.chensoul.system.infrastructure.security.mfa.config.UserMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProvider;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getCurrentUser;
import com.chensoul.system.user.domain.User;
import com.chensoul.system.user.domain.UserSetting;
import com.chensoul.system.user.domain.UserSettingType;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MfaSettingServiceImpl implements MfaSettingService {
    private static final BusinessException PROVIDER_NOT_CONFIGURED_ERROR = new BusinessException("2FA provider is not configured");
    private static final BusinessException PROVIDER_NOT_AVAILABLE_ERROR = new BusinessException("2FA provider is not available");
    private static final BusinessException TOO_MANY_REQUESTS_ERROR = new BusinessException("Too many requests");
    private final Map<MfaProviderType, MfaProvider<MfaProviderConfig, MfaConfig>> providers = new EnumMap<>(MfaProviderType.class);
    private final SystemSettingService systemSettingService;
    private final SecuritySettingService securitySettingService;
    private final UserSettingDao userSettingDao;
    private final RateLimitService rateLimitService;
    private final UserService userService;

    @Autowired
    private void setProviders(Collection<MfaProvider> providers) {
        providers.forEach(provider -> {
            this.providers.put(provider.getType(), provider);
        });
    }

    @Override
    public void checkProvider(String tenantId, MfaProviderType providerType) {
        getTwoFaProvider(providerType).check(tenantId);
    }

    @Override
    public void prepareVerificationCode(SecurityUser user, MfaConfig mfaConfig, boolean checkLimits) {
        MfaSetting twoFaSystemSettings = getSystemMfaSetting(true)
            .orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);
        if (checkLimits) {
            Integer minVerificationCodeSendPeriod = twoFaSystemSettings.getMinVerificationCodeSendPeriod();
            String rateLimit = null;
            if (minVerificationCodeSendPeriod != null && minVerificationCodeSendPeriod > 4) {
                rateLimit = "1:" + minVerificationCodeSendPeriod;
            }
            if (rateLimitService.checkRateLimited(LimitedApi.TWO_FA_VERIFICATION_CODE_SEND,
                Pair.of(user.getId(), mfaConfig.getProviderType()), rateLimit)) {
                throw TOO_MANY_REQUESTS_ERROR;
            }
        }

        MfaProviderConfig providerConfig = twoFaSystemSettings.getProviderConfig(mfaConfig.getProviderType())
            .orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);
        getTwoFaProvider(mfaConfig.getProviderType()).prepareVerificationCode(user, providerConfig, mfaConfig);
    }


    @Override
    public Optional<UserMfaConfig> getUserMfaConfig(Long userId) {
        MfaSetting twoFaSystemSetting = getSystemMfaSetting(true).orElse(null);
        return Optional.ofNullable(userSettingDao.findByUserIdAndType(userId, UserSettingType.MFA))
            .map(userAuthSettings -> {
                UserMfaConfig twoFaSettings = JacksonUtils.fromBytes(userAuthSettings.getExtraBytes(), UserMfaConfig.class);
                if (twoFaSettings == null) return null;
                boolean updateNeeded;

                Map<MfaProviderType, MfaConfig> configs = twoFaSettings.getConfigs();
                updateNeeded = configs.keySet().removeIf(providerType -> {
                    return twoFaSystemSetting == null || !twoFaSystemSetting.getProviderConfig(providerType).isPresent();
                });
                if (configs.size() == 1 && configs.containsKey(MfaProviderType.BACKUP_CODE)) {
                    configs.remove(MfaProviderType.BACKUP_CODE);
                    updateNeeded = true;
                }
                if (!configs.isEmpty() && configs.values().stream().noneMatch(MfaConfig::isUseByDefault)) {
                    configs.values().stream()
                        .filter(config -> config.getProviderType() != MfaProviderType.BACKUP_CODE)
                        .findFirst().ifPresent(config -> config.setUseByDefault(true));
                    updateNeeded = true;
                }

                if (updateNeeded) {
                    twoFaSettings = saveAccountTwoFaSettings(userId, twoFaSettings);
                }
                return twoFaSettings;
            });
    }


    @Override
    public Optional<MfaConfig> getUserMfaConfig(Long userId, MfaProviderType providerType) {
        return getUserMfaConfig(userId)
            .map(UserMfaConfig::getConfigs)
            .flatMap(configs -> Optional.ofNullable(configs.get(providerType)));
    }

    @Override
    public UserMfaConfig saveUserMfaConfig(Long userId, MfaConfig accountConfig) {
        getTwoFaProviderConfig(accountConfig.getProviderType());

        UserMfaConfig settings = getUserMfaConfig(userId).orElseGet(() -> {
            UserMfaConfig newSettings = new UserMfaConfig();
            newSettings.setConfigs(new LinkedHashMap<>());
            return newSettings;
        });
        Map<MfaProviderType, MfaConfig> configs = settings.getConfigs();
        if (configs.isEmpty() && accountConfig.getProviderType() == MfaProviderType.BACKUP_CODE) {
            throw new IllegalArgumentException("To use 2FA backup codes you first need to configure at least one provider");
        }
        if (accountConfig.isUseByDefault()) {
            configs.values().forEach(config -> config.setUseByDefault(false));
        }
        configs.put(accountConfig.getProviderType(), accountConfig);
        if (configs.values().stream().noneMatch(MfaConfig::isUseByDefault)) {
            configs.values().stream().findFirst().ifPresent(config -> config.setUseByDefault(true));
        }
        return saveAccountTwoFaSettings(userId, settings);
    }

    @Override
    public UserMfaConfig verifyAndSaveUserMfaConfig(SecurityUser user, MfaConfig accountConfig, String verificationCode) {
        if (getUserMfaConfig(user.getId(), accountConfig.getProviderType()).isPresent()) {
            throw new IllegalArgumentException("2FA provider is already configured");
        }

        boolean verificationSuccess;
        if (accountConfig.getProviderType() != MfaProviderType.BACKUP_CODE) {
            verificationSuccess = checkVerificationCode(getCurrentUser(), verificationCode, accountConfig, false);
        } else {
            verificationSuccess = true;
        }
        if (verificationSuccess) {
            return saveUserMfaConfig(user.getId(), accountConfig);
        } else {
            throw new IllegalArgumentException("Verification code is incorrect");
        }
    }

    @Override
    public UserMfaConfig updateUserMfaConfig(Long userId, MfaProviderType providerType, MfaConfigRequest updateRequest) {
        MfaConfig accountConfig = getUserMfaConfig(userId, providerType)
            .orElseThrow(() -> new IllegalArgumentException("Config for " + providerType + " 2FA provider not found"));
        accountConfig.setUseByDefault(updateRequest.isUseByDefault());
        return saveUserMfaConfig(userId, accountConfig);
    }

    @Override
    public UserMfaConfig deleteUserMfaConfig(Long userId, MfaProviderType providerType) {
        UserMfaConfig settings = getUserMfaConfig(userId)
            .orElseThrow(() -> new IllegalArgumentException("2FA not configured"));
        settings.getConfigs().remove(providerType);
        if (settings.getConfigs().size() == 1) {
            settings.getConfigs().remove(MfaProviderType.BACKUP_CODE);
        }
        if (!settings.getConfigs().isEmpty() && settings.getConfigs().values().stream()
            .noneMatch(MfaConfig::isUseByDefault)) {
            settings.getConfigs().values().stream()
                .min(Comparator.comparing(MfaConfig::getProviderType))
                .ifPresent(config -> config.setUseByDefault(true));
        }
        return saveAccountTwoFaSettings(userId, settings);
    }

    @Override
    public boolean checkVerificationCode(SecurityUser user, String verificationCode, MfaConfig accountConfig, boolean checkLimits) {
        if (!userService.findUserCredentialByUserId(user.getId()).isEnabled()) {
            throw new BusinessException("User is disabled");
        }

        MfaSetting twoFaSystemSettings = getSystemMfaSetting(true).orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);
        if (checkLimits) {
            if (rateLimitService.checkRateLimited(LimitedApi.TWO_FA_VERIFICATION_CODE_CHECK,
                Pair.of(user.getId(), accountConfig.getProviderType()), twoFaSystemSettings.getVerificationCodeCheckRateLimit())) {
                throw TOO_MANY_REQUESTS_ERROR;
            }
        }
        MfaProviderConfig providerConfig = twoFaSystemSettings.getProviderConfig(accountConfig.getProviderType())
            .orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);

        boolean verificationSuccess = false;
        if (StringUtils.isNotBlank(verificationCode)) {
            if (StringUtils.isNumeric(verificationCode) || accountConfig.getProviderType() == MfaProviderType.BACKUP_CODE) {
                verificationSuccess = getTwoFaProvider(accountConfig.getProviderType()).checkVerificationCode(user, verificationCode, providerConfig, accountConfig);
            }
        }
        if (checkLimits) {
            try {
                securitySettingService.validateMfaVerification(user, verificationSuccess, twoFaSystemSettings);
            } catch (LockedException e) {
                cleanUpRateLimits(user.getId());
                throw new BusinessException(e.getMessage());
            }
            if (verificationSuccess) {
                cleanUpRateLimits(user.getId());
            }
        }
        return verificationSuccess;
    }

    @Override
    public MfaConfig generateUserMfaConfig(User user, MfaProviderType providerType) {
        MfaProviderConfig providerConfig = getTwoFaProviderConfig(providerType);
        return getTwoFaProvider(providerType).generateTwoFaConfig(user, providerConfig);
    }

    @Override
    public Optional<MfaSetting> getSystemMfaSetting(boolean asDefault) {
        return Optional.ofNullable(systemSettingService.findSystemSettingByType(SYS_TENANT_ID, MFA))
            .map(adminSettings -> JacksonUtils.treeToValue(adminSettings.getExtra(), MfaSetting.class));
    }

    @Override
    public MfaSetting saveSystemMfaSetting(String tenantId, MfaSetting twoFaSystemSetting) {
        for (MfaProviderConfig providerConfig : twoFaSystemSetting.getProviders()) {
            checkProvider(tenantId, providerConfig.getProviderType());
        }

        SystemSetting settings = Optional.ofNullable(systemSettingService.findSystemSettingByType(tenantId, MFA))
            .orElseGet(() -> {
                SystemSetting newSettings = new SystemSetting();
                newSettings.setType(MFA);
                return newSettings;
            });
        settings.setExtra(JacksonUtils.valueToTree(twoFaSystemSetting));
        systemSettingService.saveSystemSetting(tenantId, settings);
        return twoFaSystemSetting;
    }

    @Override
    public void deleteSystemMfaSetting(String tenantId) {
        systemSettingService.deleteSystemSettingByTenantIdAndType(tenantId, MFA);
    }

    private MfaProvider<MfaProviderConfig, MfaConfig> getTwoFaProvider(MfaProviderType providerType) {
        return Optional.ofNullable(providers.get(providerType)).orElseThrow(() -> PROVIDER_NOT_AVAILABLE_ERROR);
    }

    private MfaProviderConfig getTwoFaProviderConfig(MfaProviderType providerType) {
        return getSystemMfaSetting(true)
            .flatMap(twoFaSettings -> twoFaSettings.getProviderConfig(providerType))
            .orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);
    }

    private void cleanUpRateLimits(Long userId) {
        for (MfaProviderType providerType : MfaProviderType.values()) {
            rateLimitService.cleanUp(LimitedApi.TWO_FA_VERIFICATION_CODE_SEND, Pair.of(userId, providerType));
            rateLimitService.cleanUp(LimitedApi.TWO_FA_VERIFICATION_CODE_CHECK, Pair.of(userId, providerType));
        }
    }

    protected UserMfaConfig saveAccountTwoFaSettings(Long userId, UserMfaConfig settings) {
        UserSetting userAuthSettings = Optional.ofNullable(userSettingDao.findByUserIdAndType(userId, UserSettingType.MFA))
            .orElseGet(() -> {
                UserSetting newUserAuthSettings = new UserSetting();
                newUserAuthSettings.setUserId(userId);
                newUserAuthSettings.setType(UserSettingType.MFA);
                return newUserAuthSettings;
            });
        settings.getConfigs().values().forEach(accountConfig -> accountConfig.setSerializeHiddenFields(true));
        userAuthSettings.setExtra(JacksonUtils.readTree(JacksonUtils.toString(settings)));
        userSettingDao.save(userAuthSettings);
        settings.getConfigs().values().forEach(accountConfig -> accountConfig.setSerializeHiddenFields(false));
        return settings;
    }

}
