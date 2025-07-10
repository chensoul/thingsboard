/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
