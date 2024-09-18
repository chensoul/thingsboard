package com.chensoul.system.domain.setting.domain;

import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderConfig;
import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfaSetting {
    @Valid
    @NotNull
    private List<MfaProviderConfig> providers;

    @NotNull
    @Min(value = 5)
    private Integer minVerificationCodeSendPeriod;
    @Pattern(regexp = "[1-9]\\d*:[1-9]\\d*", message = "is invalid")
    private String verificationCodeCheckRateLimit;
    @Min(value = 0, message = "must be positive")
    private Integer maxVerificationFailuresBeforeUserLockout;
    @NotNull
    @Min(value = 60)
    private Integer totalAllowedTimeForVerification;

    public Optional<MfaProviderConfig> getProviderConfig(MfaProviderType providerType) {
        return Optional.ofNullable(providers)
            .flatMap(providersConfigs -> providersConfigs.stream()
                .filter(providerConfig -> providerConfig.getProviderType() == providerType)
                .findFirst());
    }

}
