package com.chensoul.system.domain.user.mybatis;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.user.domain.UserSetting;
import com.chensoul.system.user.domain.UserSettingType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
@TableName(value = "user_setting", autoResultMap = true)
public class UserSettingEntity extends LongBaseEntity<UserSetting> {

    private Long userId;

    private UserSettingType type;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private JsonNode extra;

    @Override
    public UserSetting toData() {
        return JacksonUtils.convertValue(this, UserSetting.class);
    }
}
