package com.chensoul.system.domain.tenant.domain;

import com.chensoul.data.model.ContactBased;
import com.chensoul.data.model.HasName;
import com.chensoul.data.model.HasTenantId;
import com.chensoul.data.validation.NoXss;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.checkerframework.checker.units.qual.Length;

@Data
@EqualsAndHashCode(callSuper = true)
public class Tenant extends ContactBased<String> implements HasTenantId, HasName {
    private static final long serialVersionUID = 8057243243859922101L;

    @NoXss
    @Length
    @NotBlank(message = "Name should be specified")
    private String name;

    private Long tenantProfileId;

    @Override
    @JsonIgnore
    public String getTenantId() {
        return id;
    }
}
