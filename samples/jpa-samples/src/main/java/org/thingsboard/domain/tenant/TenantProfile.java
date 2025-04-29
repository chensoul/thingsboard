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
package org.thingsboard.domain.tenant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.model.ExtraBaseData;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

@Data
@Slf4j
public class TenantProfile extends ExtraBaseData<Long> implements HasName {

    private static final long serialVersionUID = 2628320657987010348L;

    @NoXss
    @Length
    @NotBlank(message = "Tenant profile name should be specified")
    private String name;

    @NoXss
    @Length
    private String description;

    private boolean defaulted;

    @JsonIgnore
    public Optional<DefaultTenantProfileConfiguration> getProfileConfiguration() {
        return Optional.ofNullable(getProfileData().getConfiguration())
                .filter(profileConfiguration -> profileConfiguration instanceof DefaultTenantProfileConfiguration)
                .map(profileConfiguration -> (DefaultTenantProfileConfiguration) profileConfiguration);
    }

    @JsonIgnore
    public TenantProfileData getProfileData() {
        if (extraBytes!=null) {
            try {
                return mapper.readValue(new ByteArrayInputStream(extraBytes), TenantProfileData.class);
            } catch (IOException e) {
                log.warn("Can't deserialize tenant profile data: ", e);
                return createDefaultTenantProfileData();
            }
        } else {
            return createDefaultTenantProfileData();
        }
    }

    @JsonIgnore
    public DefaultTenantProfileConfiguration getDefaultProfileConfiguration() {
        return getProfileConfiguration().orElse(null);
    }

    @JsonIgnore
    public TenantProfileData createDefaultTenantProfileData() {
        TenantProfileData tpd = new TenantProfileData();
        tpd.setConfiguration(new DefaultTenantProfileConfiguration());
        return tpd;
    }
}
