package com.chensoul.system.infrastructure.security.mfa;

import com.chensoul.exception.BusinessException;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.chensoul.system.domain.setting.domain.MfaProviderInfo;
import com.chensoul.system.infrastructure.security.jwt.token.JwtPair;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import java.util.List;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface MfaAuthService {
    boolean isMfaEnabled(Long userId);

    void prepareVerificationCode(SecurityUser user, MfaProviderType providerType, boolean checkLimits) throws Exception;

    JwtPair checkVerificationCode(SecurityUser user, String verificationCode, MfaProviderType providerType, boolean checkLimits) throws BusinessException;

    List<MfaProviderInfo> getAvailableMfaProviders();
}
