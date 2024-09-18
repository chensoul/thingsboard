package com.chensoul.system.domain.user.mybatis;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.user.domain.Authority;
import com.chensoul.system.user.domain.User;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user", autoResultMap = true)
public class UserEntity extends LongBaseEntity<User> {
    private String email;

    private String name;

    private String phone;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private JsonNode extra;

    private String tenantId;

    private Long merchantId;

    private Authority authority;

    @Override
    public User toData() {
        return JacksonUtils.convertValue(this, User.class);
    }
}
