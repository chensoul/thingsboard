/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.setting.mfa.provider.impl;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.setting.mfa.config.EmailTwoFaConfig;
import org.thingsboard.domain.setting.mfa.provider.EmailTwoFaProviderConfig;
import org.thingsboard.domain.setting.mfa.provider.TwoFaProviderType;
import org.thingsboard.domain.user.model.User;
import org.thingsboard.domain.notification.channel.mail.MailService;
import org.thingsboard.server.security.SecurityUser;

@Component
public class EmailTwoFaProvider extends OtpBasedTwoFaProvider<EmailTwoFaProviderConfig, EmailTwoFaConfig> {
	private final MailService mailService;

	protected EmailTwoFaProvider(StringRedisTemplate redisTemplate, MailService mailService) {
		super(redisTemplate);
		this.mailService = mailService;
	}

	@Override
	public EmailTwoFaConfig generateTwoFaConfig(User user, EmailTwoFaProviderConfig providerConfig) {
		EmailTwoFaConfig config = new EmailTwoFaConfig();
		config.setEmail(user.getEmail());
		return config;
	}

	@Override
	public void check(String tenantId) {
		try {
			mailService.testConnection(tenantId);
		} catch (Exception e) {
			throw new ThingsboardException("Mail service is not set up", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
		}
	}

	@Override
	protected void sendVerificationCode(SecurityUser user, String verificationCode, EmailTwoFaProviderConfig providerConfig, EmailTwoFaConfig accountConfig) {
		mailService.sendTwoFaVerificationEmail(accountConfig.getEmail(), verificationCode, providerConfig.getVerificationCodeLifetime());
	}

	@Override
	public TwoFaProviderType getType() {
		return TwoFaProviderType.EMAIL;
	}

}
