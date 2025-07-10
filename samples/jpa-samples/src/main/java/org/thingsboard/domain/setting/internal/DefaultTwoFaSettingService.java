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
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.setting.TwoFaConfigUpdateRequest;
import org.thingsboard.domain.setting.TwoFaSettingService;
import org.thingsboard.domain.setting.TwoFaSystemSetting;
import org.thingsboard.domain.usage.limit.LimitedApi;
import org.thingsboard.domain.usage.limit.RateLimitService;
import org.thingsboard.domain.setting.internal.mfa.config.TwoFaConfig;
import org.thingsboard.domain.setting.internal.mfa.config.UserTwoFaSetting;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProvider;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderConfig;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderType;
import org.thingsboard.domain.setting.SecuritySettingService;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.SystemSettingService;
import static org.thingsboard.domain.setting.SystemSettingType.MFA;
import org.thingsboard.domain.user.User;
import org.thingsboard.domain.user.UserSetting;
import org.thingsboard.domain.user.UserSettingType;
import org.thingsboard.domain.user.internal.persistence.UserSettingDao;
import org.thingsboard.domain.user.UserService;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class DefaultTwoFaSettingService implements TwoFaSettingService {
	private final Map<TwoFaProviderType, TwoFaProvider<TwoFaProviderConfig, TwoFaConfig>> providers = new EnumMap<>(TwoFaProviderType.class);
	private static final ThingsboardException PROVIDER_NOT_CONFIGURED_ERROR = new ThingsboardException("2FA provider is not configured", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
	private static final ThingsboardException PROVIDER_NOT_AVAILABLE_ERROR = new ThingsboardException("2FA provider is not available", ThingsboardErrorCode.GENERAL);
	private static final ThingsboardException TOO_MANY_REQUESTS_ERROR = new ThingsboardException("Too many requests", ThingsboardErrorCode.TOO_MANY_REQUESTS);

	private final SystemSettingService systemSettingService;
	private final SecuritySettingService securitySettingService;
	private final UserSettingDao userSettingDao;
	private final RateLimitService rateLimitService;
	private final UserService userService;

	@Autowired
	private void setProviders(Collection<TwoFaProvider> providers) {
		providers.forEach(provider -> {
			this.providers.put(provider.getType(), provider);
		});
	}

	@Override
	public void checkProvider(String tenantId, TwoFaProviderType providerType) {
		getTwoFaProvider(providerType).check(tenantId);
	}

	@Override
	public void prepareVerificationCode(SecurityUser user, TwoFaConfig twoFaConfig, boolean checkLimits) {
		TwoFaSystemSetting twoFaSystemSettings = getTwoFaSystemSetting(true)
			.orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);
		if (checkLimits) {
			Integer minVerificationCodeSendPeriod = twoFaSystemSettings.getMinVerificationCodeSendPeriod();
			String rateLimit = null;
			if (minVerificationCodeSendPeriod != null && minVerificationCodeSendPeriod > 4) {
				rateLimit = "1:" + minVerificationCodeSendPeriod;
			}
			if (rateLimitService.checkRateLimited(LimitedApi.TWO_FA_VERIFICATION_CODE_SEND,
				Pair.of(user.getId(), twoFaConfig.getProviderType()), rateLimit)) {
				throw TOO_MANY_REQUESTS_ERROR;
			}
		}

		TwoFaProviderConfig providerConfig = twoFaSystemSettings.getProviderConfig(twoFaConfig.getProviderType())
			.orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);
		getTwoFaProvider(twoFaConfig.getProviderType()).prepareVerificationCode(user, providerConfig, twoFaConfig);
	}


	@Override
	public Optional<UserTwoFaSetting> getUserTwoFaSetting(Long userId) {
		TwoFaSystemSetting twoFaSystemSetting = getTwoFaSystemSetting(true).orElse(null);
		return Optional.ofNullable(userSettingDao.findByUserIdAndType(userId, UserSettingType.MFA))
			.map(userAuthSettings -> {
				UserTwoFaSetting twoFaSettings = JacksonUtil.fromBytes(userAuthSettings.getExtraBytes(), UserTwoFaSetting.class);
				if (twoFaSettings == null) return null;
				boolean updateNeeded;

				Map<TwoFaProviderType, TwoFaConfig> configs = twoFaSettings.getConfigs();
				updateNeeded = configs.keySet().removeIf(providerType -> {
					return twoFaSystemSetting == null || twoFaSystemSetting.getProviderConfig(providerType).isEmpty();
				});
				if (configs.size() == 1 && configs.containsKey(TwoFaProviderType.BACKUP_CODE)) {
					configs.remove(TwoFaProviderType.BACKUP_CODE);
					updateNeeded = true;
				}
				if (!configs.isEmpty() && configs.values().stream().noneMatch(TwoFaConfig::isUseByDefault)) {
					configs.values().stream()
						.filter(config -> config.getProviderType() != TwoFaProviderType.BACKUP_CODE)
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
	public Optional<TwoFaConfig> getUserTwoFaConfig(Long userId, TwoFaProviderType providerType) {
		return getUserTwoFaSetting(userId)
			.map(UserTwoFaSetting::getConfigs)
			.flatMap(configs -> Optional.ofNullable(configs.get(providerType)));
	}

	@Override
	public UserTwoFaSetting saveUserTwoFaConfig(Long userId, TwoFaConfig accountConfig) {
		getTwoFaProviderConfig(accountConfig.getProviderType());

		UserTwoFaSetting settings = getUserTwoFaSetting(userId).orElseGet(() -> {
			UserTwoFaSetting newSettings = new UserTwoFaSetting();
			newSettings.setConfigs(new LinkedHashMap<>());
			return newSettings;
		});
		Map<TwoFaProviderType, TwoFaConfig> configs = settings.getConfigs();
		if (configs.isEmpty() && accountConfig.getProviderType() == TwoFaProviderType.BACKUP_CODE) {
			throw new IllegalArgumentException("To use 2FA backup codes you first need to configure at least one provider");
		}
		if (accountConfig.isUseByDefault()) {
			configs.values().forEach(config -> config.setUseByDefault(false));
		}
		configs.put(accountConfig.getProviderType(), accountConfig);
		if (configs.values().stream().noneMatch(TwoFaConfig::isUseByDefault)) {
			configs.values().stream().findFirst().ifPresent(config -> config.setUseByDefault(true));
		}
		return saveAccountTwoFaSettings(userId, settings);
	}

	@Override
	public UserTwoFaSetting verifyAndSaveUserTwoFaConfig(SecurityUser user, TwoFaConfig accountConfig, String verificationCode) {
		if (getUserTwoFaConfig(user.getId(), accountConfig.getProviderType()).isPresent()) {
			throw new IllegalArgumentException("2FA provider is already configured");
		}

		boolean verificationSuccess;
		if (accountConfig.getProviderType() != TwoFaProviderType.BACKUP_CODE) {
			verificationSuccess = checkVerificationCode(getCurrentUser(), verificationCode, accountConfig, false);
		} else {
			verificationSuccess = true;
		}
		if (verificationSuccess) {
			return saveUserTwoFaConfig(user.getId(), accountConfig);
		} else {
			throw new IllegalArgumentException("Verification code is incorrect");
		}
	}

	@Override
	public UserTwoFaSetting updateUserTwoFaConfig(Long userId, TwoFaProviderType providerType, TwoFaConfigUpdateRequest updateRequest) {
		TwoFaConfig accountConfig = getUserTwoFaConfig(userId, providerType)
			.orElseThrow(() -> new IllegalArgumentException("Config for " + providerType + " 2FA provider not found"));
		accountConfig.setUseByDefault(updateRequest.isUseByDefault());
		return saveUserTwoFaConfig(userId, accountConfig);
	}

	@Override
	public UserTwoFaSetting deleteUserTwoFaConfig(Long userId, TwoFaProviderType providerType) {
		UserTwoFaSetting settings = getUserTwoFaSetting(userId)
			.orElseThrow(() -> new IllegalArgumentException("2FA not configured"));
		settings.getConfigs().remove(providerType);
		if (settings.getConfigs().size() == 1) {
			settings.getConfigs().remove(TwoFaProviderType.BACKUP_CODE);
		}
		if (!settings.getConfigs().isEmpty() && settings.getConfigs().values().stream()
			.noneMatch(TwoFaConfig::isUseByDefault)) {
			settings.getConfigs().values().stream()
				.min(Comparator.comparing(TwoFaConfig::getProviderType))
				.ifPresent(config -> config.setUseByDefault(true));
		}
		return saveAccountTwoFaSettings(userId, settings);
	}

	@Override
	public boolean checkVerificationCode(SecurityUser user, String verificationCode, TwoFaConfig accountConfig, boolean checkLimits) {
		if (!userService.findUserCredentialByUserId(user.getId()).isEnabled()) {
			throw new ThingsboardException("User is disabled", ThingsboardErrorCode.AUTHENTICATION);
		}

		TwoFaSystemSetting twoFaSystemSettings = getTwoFaSystemSetting(true).orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);
		if (checkLimits) {
			if (rateLimitService.checkRateLimited(LimitedApi.TWO_FA_VERIFICATION_CODE_CHECK,
				Pair.of(user.getId(), accountConfig.getProviderType()), twoFaSystemSettings.getVerificationCodeCheckRateLimit())) {
				throw TOO_MANY_REQUESTS_ERROR;
			}
		}
		TwoFaProviderConfig providerConfig = twoFaSystemSettings.getProviderConfig(accountConfig.getProviderType())
			.orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);

		boolean verificationSuccess = false;
		if (StringUtils.isNotBlank(verificationCode)) {
			if (StringUtils.isNumeric(verificationCode) || accountConfig.getProviderType() == TwoFaProviderType.BACKUP_CODE) {
				verificationSuccess = getTwoFaProvider(accountConfig.getProviderType()).checkVerificationCode(user, verificationCode, providerConfig, accountConfig);
			}
		}
		if (checkLimits) {
			try {
				securitySettingService.validateTwoFaVerification(user, verificationSuccess, twoFaSystemSettings);
			} catch (LockedException e) {
				cleanUpRateLimits(user.getId());
				throw new ThingsboardException(e.getMessage(), ThingsboardErrorCode.AUTHENTICATION);
			}
			if (verificationSuccess) {
				cleanUpRateLimits(user.getId());
			}
		}
		return verificationSuccess;
	}

	@Override
	public TwoFaConfig generateUserTwoFaConfig(User user, TwoFaProviderType providerType) {
		TwoFaProviderConfig providerConfig = getTwoFaProviderConfig(providerType);
		return getTwoFaProvider(providerType).generateTwoFaConfig(user, providerConfig);
	}

	@Override
	public Optional<TwoFaSystemSetting> getTwoFaSystemSetting(boolean asDefault) {
		return Optional.ofNullable(systemSettingService.findSystemSettingByType(SYS_TENANT_ID, MFA))
			.map(adminSettings -> JacksonUtil.treeToValue(adminSettings.getExtra(), TwoFaSystemSetting.class));
	}

	@Override
	public TwoFaSystemSetting saveTwoFaSystemSetting(String tenantId, TwoFaSystemSetting twoFaSystemSetting) {
		for (TwoFaProviderConfig providerConfig : twoFaSystemSetting.getProviders()) {
			checkProvider(tenantId, providerConfig.getProviderType());
		}

		SystemSetting settings = Optional.ofNullable(systemSettingService.findSystemSettingByType(tenantId, MFA))
			.orElseGet(() -> {
				SystemSetting newSettings = new SystemSetting();
				newSettings.setType(MFA);
				return newSettings;
			});
		settings.setExtra(JacksonUtil.valueToTree(twoFaSystemSetting));
		systemSettingService.saveSystemSetting(tenantId, settings);
		return twoFaSystemSetting;
	}

	@Override
	public void deleteTwoFaSystemSetting(String tenantId) {
		Optional.ofNullable(systemSettingService.findSystemSettingByType(tenantId, MFA))
			.ifPresent(adminSettings -> systemSettingService.deleteSystemSettingById(adminSettings.getId()));
	}

	private TwoFaProvider<TwoFaProviderConfig, TwoFaConfig> getTwoFaProvider(TwoFaProviderType providerType) {
		return Optional.ofNullable(providers.get(providerType)).orElseThrow(() -> PROVIDER_NOT_AVAILABLE_ERROR);
	}

	private TwoFaProviderConfig getTwoFaProviderConfig(TwoFaProviderType providerType) {
		return getTwoFaSystemSetting(true)
			.flatMap(twoFaSettings -> twoFaSettings.getProviderConfig(providerType))
			.orElseThrow(() -> PROVIDER_NOT_CONFIGURED_ERROR);
	}

	private void cleanUpRateLimits(Long userId) {
		for (TwoFaProviderType providerType : TwoFaProviderType.values()) {
			rateLimitService.cleanUp(LimitedApi.TWO_FA_VERIFICATION_CODE_SEND, Pair.of(userId, providerType));
			rateLimitService.cleanUp(LimitedApi.TWO_FA_VERIFICATION_CODE_CHECK, Pair.of(userId, providerType));
		}
	}

	protected UserTwoFaSetting saveAccountTwoFaSettings(Long userId, UserTwoFaSetting settings) {
		UserSetting userAuthSettings = Optional.ofNullable(userSettingDao.findByUserIdAndType(userId, UserSettingType.MFA))
			.orElseGet(() -> {
				UserSetting newUserAuthSettings = new UserSetting();
				newUserAuthSettings.setUserId(userId);
				newUserAuthSettings.setType(UserSettingType.MFA);
				return newUserAuthSettings;
			});
		settings.getConfigs().values().forEach(accountConfig -> accountConfig.setSerializeHiddenFields(true));
		userAuthSettings.setExtra(JacksonUtil.readTree(JacksonUtil.toString(settings)));
		userSettingDao.save(userAuthSettings);
		settings.getConfigs().values().forEach(accountConfig -> accountConfig.setSerializeHiddenFields(false));
		return settings;
	}

}
