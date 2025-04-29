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
package com.chensoul.system.domain.user.service.impl;

import com.chensoul.data.model.HasId;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.user.mybatis.UserSettingDao;
import com.chensoul.system.domain.user.service.UserSettingService;
import com.chensoul.system.domain.user.service.UserSettingValidator;
import com.chensoul.system.user.domain.UserSetting;
import com.chensoul.system.user.domain.UserSettingType;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl implements UserSettingService {
    private final UserSettingDao userSettingDao;
    private final UserSettingValidator userValidator;

    @Override
    public UserSetting saveUserSetting(UserSetting userSetting) {
        UserSetting old = userValidator.validate(userSetting);
        return userSettingDao.save(userSetting);
    }

    @Override
    public UserSetting findUserSettingById(Long id) {
        return userSettingDao.findById(id);
    }

    @Override
    public List<UserSetting> findUserSettingByUserId(Long userId) {
        return userSettingDao.findByUserId(userId);
    }

    @Override
    public UserSetting findUserSettingByUserIdAndType(Long userId, UserSettingType type) {
        return userSettingDao.findByUserIdAndType(userId, type);
    }

    @Override
    public void deleteUserSetting(UserSetting userSetting) {
        userValidator.validateDelete(userSetting);
        userSettingDao.removeById(userSetting.getId());
    }

    @Override
    public Optional<HasId> findEntity(Serializable id) {
        return Optional.ofNullable(findUserSettingById((Long) id));
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.USER_SETTING;
    }
}
