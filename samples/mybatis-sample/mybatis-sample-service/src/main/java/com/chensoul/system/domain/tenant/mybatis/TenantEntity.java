package com.chensoul.system.domain.tenant.mybatis;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.StringBaseEntity;
import com.chensoul.system.domain.tenant.domain.Tenant;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tenant", autoResultMap = true)
public final class TenantEntity extends StringBaseEntity<Tenant> {
    private String name;

    private String country;

    private String state;

    private String city;

    private String address;

    private String address2;

    private String zip;

    private String phone;

    private String email;

    private Long tenantProfileId;

    @Override
    public Tenant toData() {
        return JacksonUtils.convertValue(this, Tenant.class);
    }
}
