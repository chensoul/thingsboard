package com.chensoul.system.infrastructure.security.mfa.config;

import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import java.util.LinkedHashMap;
import lombok.Data;

@Data
public class UserMfaConfig {
    private LinkedHashMap<MfaProviderType, MfaConfig> configs;
}
