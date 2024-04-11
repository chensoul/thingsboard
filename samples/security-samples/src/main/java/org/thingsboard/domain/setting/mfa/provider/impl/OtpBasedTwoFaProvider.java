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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import static org.thingsboard.common.CacheConstants.TWO_FA_VERIFICATION_CODE_CACHE;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.setting.mfa.config.OtpBasedTwoFaConfig;
import org.thingsboard.domain.setting.mfa.provider.OtpBasedTwoFaProviderConfig;
import org.thingsboard.domain.setting.mfa.provider.TwoFaProvider;
import org.thingsboard.server.security.SecurityUser;

@Component
@RequiredArgsConstructor
public abstract class OtpBasedTwoFaProvider<C extends OtpBasedTwoFaProviderConfig, A extends OtpBasedTwoFaConfig> implements TwoFaProvider<C, A> {
	private final StringRedisTemplate redisTemplate;

	@Override
	public final void prepareVerificationCode(SecurityUser user, C providerConfig, A accountConfig) {
		String verificationCode = RandomStringUtils.randomNumeric(6);
		sendVerificationCode(user, verificationCode, providerConfig, accountConfig);
		redisTemplate.opsForValue().set(TWO_FA_VERIFICATION_CODE_CACHE + ":" + user.getId(), JacksonUtil.toString(new Otp(System.currentTimeMillis(), verificationCode, accountConfig)));
	}

	protected abstract void sendVerificationCode(SecurityUser user, String verificationCode, C providerConfig, A accountConfig) throws ThingsboardException;

	@Override
	public final boolean checkVerificationCode(SecurityUser user, String code, C providerConfig, A accountConfig) {
		String correctVerificationCode = redisTemplate.opsForValue().get(TWO_FA_VERIFICATION_CODE_CACHE + ":" + user.getId());
		Otp otp = JacksonUtil.fromString(correctVerificationCode, Otp.class);
		if (correctVerificationCode != null) {
			if (System.currentTimeMillis() - otp.getTimestamp()
				> TimeUnit.SECONDS.toMillis(providerConfig.getVerificationCodeLifetime())) {
				redisTemplate.delete(TWO_FA_VERIFICATION_CODE_CACHE + ":" + user.getId());
				return false;
			}
			if (code.equals(otp.getValue()) && accountConfig.equals(otp.getAccountConfig())) {
				redisTemplate.delete(TWO_FA_VERIFICATION_CODE_CACHE + ":" + user.getId());
				return true;
			}
		}
		return false;
	}


	@Data
	public static class Otp implements Serializable {
		private final long timestamp;
		private final String value;
		private final OtpBasedTwoFaConfig accountConfig;
	}

}
