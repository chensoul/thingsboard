package com.chensoul.system.infrastructure.security.mfa.provider;

import javax.validation.constraints.Min;
import lombok.Data;

@Data
public class BackupCodeMfaProviderConfig implements MfaProviderConfig {

    @Min(value = 1, message = "must be greater than 0")
    private int codesQuantity;

    @Override
    public MfaProviderType getProviderType() {
        return MfaProviderType.BACKUP_CODE;
    }

}
