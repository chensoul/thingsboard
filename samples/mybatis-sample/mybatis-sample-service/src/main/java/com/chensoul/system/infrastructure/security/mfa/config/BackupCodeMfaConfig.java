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
