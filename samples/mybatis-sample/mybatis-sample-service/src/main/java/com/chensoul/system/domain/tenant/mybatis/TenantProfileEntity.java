package com.chensoul.system.domain.tenant.mybatis;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.domain.tenant.domain.TenantProfile;
import lombok.Data;

@Data
@TableName(value = "tenant_profile")
public class TenantProfileEntity extends LongBaseEntity<TenantProfile> {

    private static final long serialVersionUID = 2628320657987010348L;

    private String name;

    private String description;

    private boolean defaulted;

    @Override
    public TenantProfile toData() {
        return JacksonUtils.convertValue(this, TenantProfile.class);
    }
}
