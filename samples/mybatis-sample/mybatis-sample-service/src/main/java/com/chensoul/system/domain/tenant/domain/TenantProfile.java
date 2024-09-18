package com.chensoul.system.domain.tenant.domain;

import com.chensoul.data.model.BaseDataWithExtra;
import com.chensoul.data.model.HasName;
import com.chensoul.data.validation.NoXss;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.Length;

@Data
@Slf4j
public class TenantProfile extends BaseDataWithExtra<Long> implements HasName {

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
        if (extraBytes != null) {
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
