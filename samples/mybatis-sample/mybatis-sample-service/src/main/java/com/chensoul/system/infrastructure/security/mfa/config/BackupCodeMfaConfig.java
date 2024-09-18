package com.chensoul.system.infrastructure.security.mfa.config;

import com.chensoul.system.infrastructure.security.mfa.provider.MfaProviderType;
import com.fasterxml.jackson.annotation.JsonGetter;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BackupCodeMfaConfig extends MfaConfig {

    @NotEmpty
    private Set<String> codes;

    @Override
    public MfaProviderType getProviderType() {
        return MfaProviderType.BACKUP_CODE;
    }

    @JsonGetter("codes")
    private Set<String> getCodesForJson() {
        if (serializeHiddenFields) {
            return codes;
        } else {
            return null;
        }
    }

    @JsonGetter
    private Integer getCodesLeft() {
        if (codes != null) {
            return codes.size();
        } else {
            return null;
        }
    }

}
