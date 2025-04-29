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
package com.chensoul.system.infrastructure.security.mfa;

import com.chensoul.exception.BusinessException;
import com.chensoul.system.infrastructure.security.mfa.config.EmailMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.config.SmsMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.config.MfaConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.chensoul.system.domain.setting.domain.MfaProviderInfo;
import com.chensoul.system.domain.setting.domain.MfaSetting;
import com.chensoul.system.domain.setting.service.MfaSettingService;
import com.chensoul.system.domain.user.service.UserService;
import com.chensoul.system.infrastructure.security.jwt.JwtTokenFactory;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getCurrentUser;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import static org.apache.commons.lang3.StringUtils.repeat;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class MfaAuthServiceImpl implements MfaAuthService {
    public static final BusinessException USER_NOT_CONFIGURED_ERROR = new BusinessException("2FA is not configured for user");

    private final UserService userService;
    private final MfaSettingService mfaSettingService;
    private final JwtTokenFactory tokenFactory;

    private static String obfuscate(String input, int seenMargin, char obfuscationChar,
                                    int startIndexInclusive, int endIndexExclusive) {
        String part = input.substring(startIndexInclusive, endIndexExclusive);
        String obfuscatedPart;
        if (part.length() <= seenMargin * 2) {
            obfuscatedPart = repeat(obfuscationChar, part.length());
        } else {
            obfuscatedPart = part.substring(0, seenMargin)
                             + repeat(obfuscationChar, part.length() - seenMargin * 2)
                             + part.substring(part.length() - seenMargin);
        }
        return input.substring(0, startIndexInclusive) + obfuscatedPart + input.substring(endIndexExclusive);
    }

    @Override
    public boolean isMfaEnabled(Long userId) {
        return mfaSettingService.getUserMfaConfig(userId).map(settings -> !settings.getConfigs().isEmpty()).orElse(false);
    }

    @Override
    public void prepareVerificationCode(SecurityUser user, MfaProviderType providerType, boolean checkLimits) throws Exception {
        MfaConfig accountConfig = mfaSettingService.getUserMfaConfig(user.getId(), providerType).orElseThrow(() -> USER_NOT_CONFIGURED_ERROR);
        mfaSettingService.prepareVerificationCode(user, accountConfig, checkLimits);
    }

    @Override
    public JwtPair checkVerificationCode(SecurityUser user, String verificationCode, MfaProviderType providerType, boolean checkLimits) {
        MfaConfig accountConfig = mfaSettingService.getUserMfaConfig(user.getId(), providerType)
            .orElseThrow(() -> USER_NOT_CONFIGURED_ERROR);
        boolean verificationSuccess = mfaSettingService.checkVerificationCode(user, verificationCode, accountConfig, checkLimits);

        if (verificationSuccess) {
            user = new SecurityUser(userService.findUserById(user.getId()), true, user.getUserPrincipal());
            return tokenFactory.createTokenPair(user);
        } else {
            throw new BusinessException("Verification code is incorrect");
        }
    }

    @Override
    public List<MfaProviderInfo> getAvailableMfaProviders() {
        SecurityUser user = getCurrentUser();
        Optional<MfaSetting> twoFaSystemSetting = mfaSettingService.getSystemMfaSetting(true);
        return mfaSettingService.getUserMfaConfig(user.getId())
            .map(settings -> settings.getConfigs().values()).orElse(Collections.emptyList())
            .stream().map(config -> {
                String contact = null;
                switch (config.getProviderType()) {
                    case SMS:
                        String phoneNumber = ((SmsMfaConfig) config).getPhoneNumber();
                        contact = obfuscate(phoneNumber, 2, '*', phoneNumber.indexOf('+') + 1, phoneNumber.length());
                        break;
                    case EMAIL:
                        String email = ((EmailMfaConfig) config).getEmail();
                        contact = obfuscate(email, 2, '*', 0, email.indexOf('@'));
                        break;
                }
                return MfaProviderInfo.builder()
                    .type(config.getProviderType())
                    .useByDefault(config.isUseByDefault())
                    .contact(contact)
                    .minVerificationCodeSendPeriod(twoFaSystemSetting.get().getMinVerificationCodeSendPeriod())
                    .build();
            })
            .collect(Collectors.toList());
    }
}
