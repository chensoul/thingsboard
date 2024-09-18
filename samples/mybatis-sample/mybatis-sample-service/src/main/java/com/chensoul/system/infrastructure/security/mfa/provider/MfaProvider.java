package com.chensoul.system.infrastructure.security.mfa.provider;


import com.chensoul.system.infrastructure.security.mfa.config.MfaConfig;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import com.chensoul.system.user.domain.User;

public interface MfaProvider<C extends MfaProviderConfig, A extends MfaConfig> {

    A generateTwoFaConfig(User user, C providerConfig);

    default void prepareVerificationCode(SecurityUser user, C providerConfig, A accountConfig) {
    }

    boolean checkVerificationCode(SecurityUser user, String code, C providerConfig, A accountConfig);

    default void check(String tenantId) {
    }

    MfaProviderType getType();

}
