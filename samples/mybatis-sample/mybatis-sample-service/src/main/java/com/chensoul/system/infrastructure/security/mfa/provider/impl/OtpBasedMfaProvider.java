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
package com.chensoul.system.infrastructure.security.mfa.provider.impl;

import com.chensoul.json.JacksonUtils;
import static com.chensoul.system.CacheConstants.TWO_FA_VERIFICATION_CODE_CACHE;
import com.chensoul.system.infrastructure.security.mfa.config.OtpBasedMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.OtpBasedMfaProviderConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProvider;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public abstract class OtpBasedMfaProvider<C extends OtpBasedMfaProviderConfig, A extends OtpBasedMfaConfig> implements MfaProvider<C, A> {
    private final CacheManager cacheManager;

    @Override
    public final void prepareVerificationCode(SecurityUser user, C providerConfig, A accountConfig) {
        String verificationCode = RandomStringUtils.randomNumeric(6);
        sendVerificationCode(user, verificationCode, providerConfig, accountConfig);
        cacheManager.getCache(TWO_FA_VERIFICATION_CODE_CACHE).put(TWO_FA_VERIFICATION_CODE_CACHE + ":" + user.getId(), JacksonUtils.toString(new Otp(System.currentTimeMillis(), verificationCode, accountConfig)));
    }

    protected abstract void sendVerificationCode(SecurityUser user, String verificationCode, C providerConfig, A accountConfig);

    @Override
    public final boolean checkVerificationCode(SecurityUser user, String code, C providerConfig, A accountConfig) {
        String correctVerificationCode = cacheManager.getCache(TWO_FA_VERIFICATION_CODE_CACHE).get(TWO_FA_VERIFICATION_CODE_CACHE + ":" + user.getId()).toString();
        Otp otp = JacksonUtils.fromString(correctVerificationCode, Otp.class);
        if (correctVerificationCode != null) {
            if (System.currentTimeMillis() - otp.getTimestamp()
                > TimeUnit.SECONDS.toMillis(providerConfig.getVerificationCodeLifetime())) {
                cacheManager.getCache(TWO_FA_VERIFICATION_CODE_CACHE).evict(TWO_FA_VERIFICATION_CODE_CACHE + ":" + user.getId());
                return false;
            }
            if (code.equals(otp.getValue()) && accountConfig.equals(otp.getAccountConfig())) {
                cacheManager.getCache(TWO_FA_VERIFICATION_CODE_CACHE).evict(TWO_FA_VERIFICATION_CODE_CACHE + ":" + user.getId());
                return true;
            }
        }
        return false;
    }


    @Data
    public static class Otp implements Serializable {
        private final long timestamp;
        private final String value;
        private final OtpBasedMfaConfig accountConfig;
    }

}
