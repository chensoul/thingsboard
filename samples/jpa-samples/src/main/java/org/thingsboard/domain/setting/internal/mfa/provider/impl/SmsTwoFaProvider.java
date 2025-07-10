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

import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.util.FormatUtils;
import org.thingsboard.domain.notification.internal.channel.sms.SmsService;
import org.thingsboard.domain.setting.internal.mfa.provider.TwoFaProviderType;
import org.thingsboard.domain.setting.internal.mfa.config.SmsTwoFaConfig;
import org.thingsboard.domain.setting.internal.mfa.provider.SmsTwoFaProviderConfig;
import org.thingsboard.domain.user.User;
import org.thingsboard.server.security.SecurityUser;

@Service
public class SmsTwoFaProvider extends OtpBasedTwoFaProvider<SmsTwoFaProviderConfig, SmsTwoFaConfig> {
	private final SmsService smsService;

	public SmsTwoFaProvider(CacheManager cacheManager, SmsService smsService) {
		super(cacheManager);
		this.smsService = smsService;
	}

	@Override
	public SmsTwoFaConfig generateTwoFaConfig(User user, SmsTwoFaProviderConfig providerConfig) {
		return new SmsTwoFaConfig();
	}

	@Override
	protected void sendVerificationCode(SecurityUser user, String verificationCode, SmsTwoFaProviderConfig providerConfig, SmsTwoFaConfig accountConfig) {
		Map<String, String> messageData = Map.of(
			"code", verificationCode,
			"userEmail", user.getEmail()
		);
		String message = FormatUtils.formatVariables(providerConfig.getSmsVerificationMessageTemplate(), "${", "}", messageData);
		String phoneNumber = accountConfig.getPhoneNumber();
		try {
			smsService.sendSms(user.getTenantId(), user.getMerchantId(), new String[]{phoneNumber}, message);
		} catch (ThingsboardException e) {
			throw e;
		}
	}

	@Override
	public void check(String tenantId) {
		if (!smsService.isConfigured(tenantId)) {
			throw new ThingsboardException("SMS service is not configured", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
	}

	@Override
	public TwoFaProviderType getType() {
		return TwoFaProviderType.SMS;
	}

}
