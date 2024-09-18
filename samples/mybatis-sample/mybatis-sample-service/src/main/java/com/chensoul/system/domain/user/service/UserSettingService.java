package com.chensoul.system.domain.user.service;

import com.chensoul.system.infrastructure.common.EntityDaoService;
import com.chensoul.system.user.domain.UserSetting;
import com.chensoul.system.user.domain.UserSettingType;
import java.util.List;

public interface UserSettingService extends EntityDaoService {

    UserSetting findUserSettingByUserIdAndType(Long userId, UserSettingType type);

    UserSetting findUserSettingById(Long id);

    UserSetting saveUserSetting(UserSetting userSetting);

    List<UserSetting> findUserSettingByUserId(Long userId);

    void deleteUserSetting(UserSetting userSetting);

}
