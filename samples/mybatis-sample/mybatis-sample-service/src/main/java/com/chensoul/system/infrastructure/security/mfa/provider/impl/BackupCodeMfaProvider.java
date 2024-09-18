package com.chensoul.system.infrastructure.security.mfa.provider.impl;

import com.chensoul.system.infrastructure.security.mfa.config.BackupCodeMfaConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.BackupCodeMfaProviderConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProvider;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.chensoul.system.domain.setting.service.MfaSettingService;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.User;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class BackupCodeMfaProvider implements MfaProvider<BackupCodeMfaProviderConfig, BackupCodeMfaConfig> {

    @Autowired
    @Lazy
    private MfaSettingService mfaSettingService;

    private static Set<String> generateCodes(int count, int length) {
        return Stream.generate(() -> RandomStringUtils.random(length, "0123456789abcdef"))
            .distinct().limit(count)
            .collect(Collectors.toSet());
    }

    @Override
    public BackupCodeMfaConfig generateTwoFaConfig(User user, BackupCodeMfaProviderConfig providerConfig) {
        BackupCodeMfaConfig config = new BackupCodeMfaConfig();
        config.setCodes(generateCodes(providerConfig.getCodesQuantity(), 8));
        config.setSerializeHiddenFields(true);
        return config;
    }

    @Override
    public boolean checkVerificationCode(SecurityUser user, String code, BackupCodeMfaProviderConfig providerConfig, BackupCodeMfaConfig accountConfig) {
        if (CollectionUtils.contains(accountConfig.getCodes().iterator(), code)) {
            accountConfig.getCodes().remove(code);
            mfaSettingService.saveUserMfaConfig(user.getId(), accountConfig);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public MfaProviderType getType() {
        return MfaProviderType.BACKUP_CODE;
    }

}
