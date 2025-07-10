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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.user.domain.UserSetting;
import com.chensoul.system.user.domain.UserSettingType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Component
@RequiredArgsConstructor
public class UserSettingDao extends AbstractDao<UserSettingEntity, UserSetting, Long> {
    private final UserSettingMapper mapper;

    @Override
    protected Class<UserSettingEntity> getEntityClass() {
        return UserSettingEntity.class;
    }

    @Override
    protected BaseMapper<UserSettingEntity> getRepository() {
        return mapper;
    }

    public void removeByUserId(Long userId) {
        mapper.delete(Wrappers.lambdaQuery(UserSettingEntity.class).eq(UserSettingEntity::getUserId, userId));
    }

    public List<UserSetting> findByUserId(Long userId) {
        return DaoUtil.convertDataList(
            mapper.selectList(Wrappers.lambdaQuery(UserSettingEntity.class)
                .eq(UserSettingEntity::getUserId, userId))
        );
    }

    public UserSetting findByUserIdAndType(Long userId, UserSettingType type) {
        return DaoUtil.getData(
            mapper.selectOne(Wrappers.lambdaQuery(UserSettingEntity.class)
                .eq(UserSettingEntity::getUserId, userId).eq(UserSettingEntity::getType, type))
        );
    }
}
