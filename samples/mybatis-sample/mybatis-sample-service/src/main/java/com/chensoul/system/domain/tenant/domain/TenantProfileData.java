package com.chensoul.system.domain.tenant.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TenantProfileData implements Serializable {

    private static final long serialVersionUID = -3642550257035920976L;

    private TenantProfileConfiguration configuration;
}
