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
package org.thingsboard.domain.setting.internal.mfa.provider.impl;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thingsboard.domain.setting.TwoFaSettingService;
import org.thingsboard.domain.setting.internal.mfa.config.BackupCodeTwoFaConfig;
import org.thingsboard.domain.setting.internal.mfa.provider.BackupCodeTwoFaProviderConfig;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProvider;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderType;
import org.thingsboard.domain.user.User;
import org.thingsboard.server.security.SecurityUser;

@Component
public class BackupCodeTwoFaProvider implements TwoFaProvider<BackupCodeTwoFaProviderConfig, BackupCodeTwoFaConfig> {

	@Autowired
	@Lazy
	private TwoFaSettingService twoFaSettingService;

	@Override
	public BackupCodeTwoFaConfig generateTwoFaConfig(User user, BackupCodeTwoFaProviderConfig providerConfig) {
		BackupCodeTwoFaConfig config = new BackupCodeTwoFaConfig();
		config.setCodes(generateCodes(providerConfig.getCodesQuantity(), 8));
		config.setSerializeHiddenFields(true);
		return config;
	}

	private static Set<String> generateCodes(int count, int length) {
		return Stream.generate(() -> RandomStringUtils.random(length, "0123456789abcdef"))
			.distinct().limit(count)
			.collect(Collectors.toSet());
	}

	@Override
	public boolean checkVerificationCode(SecurityUser user, String code, BackupCodeTwoFaProviderConfig providerConfig, BackupCodeTwoFaConfig accountConfig) {
		if (CollectionUtils.contains(accountConfig.getCodes().iterator(), code)) {
			accountConfig.getCodes().remove(code);
			twoFaSettingService.saveUserTwoFaConfig(user.getId(), accountConfig);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public TwoFaProviderType getType() {
		return TwoFaProviderType.BACKUP_CODE;
	}

}
