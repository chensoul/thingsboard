package com.chensoul.system.domain.setting.domain;

import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MfaProviderInfo {
    private MfaProviderType type;
    private boolean useByDefault;
    private String contact;
    private Integer minVerificationCodeSendPeriod;
}
