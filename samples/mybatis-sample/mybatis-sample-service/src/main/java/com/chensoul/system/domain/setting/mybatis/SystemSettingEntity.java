package com.chensoul.system.domain.setting.mybatis;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import com.chensoul.system.domain.setting.domain.SystemSettingType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "system_setting", autoResultMap = true)
public final class SystemSettingEntity extends LongBaseEntity<SystemSetting> {

    private String tenantId;

    private SystemSettingType type;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private JsonNode extra;

    @Override
    public SystemSetting toData() {
        return JacksonUtils.convertValue(this, SystemSetting.class);
    }
}
