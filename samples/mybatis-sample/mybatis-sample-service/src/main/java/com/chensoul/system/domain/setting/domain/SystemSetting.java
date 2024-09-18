package com.chensoul.system.domain.setting.domain;

import com.chensoul.data.model.BaseDataWithExtra;
import com.chensoul.data.model.HasTenantId;
import lombok.Data;

@Data
public class SystemSetting extends BaseDataWithExtra<Long> implements HasTenantId {

    private static final long serialVersionUID = -7670322981725511892L;

    private String tenantId;

    private SystemSettingType type;
}
