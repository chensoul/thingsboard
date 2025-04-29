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

import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.notification.channel.sms.SmsService;
import com.chensoul.system.infrastructure.security.mfa.config.SmsMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.SmsMfaProviderConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.User;
import com.chensoul.text.FormatUtils;
import com.chensoul.util.Maps;
import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class SmsMfaProvider extends OtpBasedMfaProvider<SmsMfaProviderConfig, SmsMfaConfig> {
    private final SmsService smsService;

    public SmsMfaProvider(CacheManager cacheManager, SmsService smsService) {
        super(cacheManager);
        this.smsService = smsService;
    }

    @Override
    public SmsMfaConfig generateTwoFaConfig(User user, SmsMfaProviderConfig providerConfig) {
        return new SmsMfaConfig();
    }

    @Override
    protected void sendVerificationCode(SecurityUser user, String verificationCode, SmsMfaProviderConfig providerConfig, SmsMfaConfig accountConfig) {
        Map<String, String> messageData = Maps.of(
            "code", verificationCode,
            "userEmail", user.getEmail()
        );
        String message = FormatUtils.formatVariables(providerConfig.getSmsVerificationMessageTemplate(), "${", "}", messageData);
        String phoneNumber = accountConfig.getPhoneNumber();
        try {
            smsService.sendSms(user.getTenantId(), user.getMerchantId(), new String[]{phoneNumber}, message);
        } catch (BusinessException e) {
            throw e;
        }
    }

    @Override
    public void check(String tenantId) {
        if (!smsService.isConfigured(tenantId)) {
            throw new BusinessException("SMS service is not configured");
        }
    }

    @Override
    public MfaProviderType getType() {
        return MfaProviderType.SMS;
    }

}
