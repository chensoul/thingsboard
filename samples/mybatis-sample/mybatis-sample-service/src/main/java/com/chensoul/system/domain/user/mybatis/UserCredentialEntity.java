package com.chensoul.system.domain.user.mybatis;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chensoul.json.JacksonUtils;
import com.chensoul.mybatis.model.LongBaseEntity;
import com.chensoul.system.user.domain.UserCredential;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_credential", autoResultMap = true)
public final class UserCredentialEntity extends LongBaseEntity<UserCredential> {

    private Long userId;

    private boolean enabled;

    private String password;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String activateToken;

    private String resetToken;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private JsonNode extra;

    @Override
    public UserCredential toData() {
        return JacksonUtils.convertValue(this, UserCredential.class);
    }
}
