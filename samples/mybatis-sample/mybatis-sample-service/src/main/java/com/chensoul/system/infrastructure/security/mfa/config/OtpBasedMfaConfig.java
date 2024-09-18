package com.chensoul.system.infrastructure.security.mfa.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class OtpBasedMfaConfig extends MfaConfig {
}
