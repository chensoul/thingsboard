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
package com.chensoul.system.infrastructure.common;

import com.chensoul.data.model.BaseData;
import com.chensoul.data.model.HasId;
import com.chensoul.data.validation.Validators;
import static com.chensoul.data.validation.Validators.validateId;
import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.audit.domain.EntityType;
import com.chensoul.system.domain.audit.service.AuditLogService;
import com.chensoul.system.domain.merchant.Merchant;
import com.chensoul.system.domain.merchant.MerchantService;
import com.chensoul.system.domain.setting.service.SystemSettingService;
import com.chensoul.system.domain.usage.limit.RateLimitService;
import com.chensoul.system.domain.user.service.AuthService;
import com.chensoul.system.domain.user.service.UserService;
import com.chensoul.system.domain.user.service.UserSettingService;
import com.chensoul.system.user.domain.User;
import com.chensoul.system.user.domain.UserSetting;
import java.io.Serializable;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
public class BaseController {
    @Autowired
    protected AuditLogService auditLogService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected MerchantService merchantService;
    @Autowired
    protected UserSettingService userSettingService;
    @Autowired
    protected AuthService authService;
    @Autowired
    protected SystemSettingService systemSettingService;
    @Autowired
    protected RateLimitService rateLimitService;

    protected <I extends Serializable, T extends BaseData<I>> void checkEntity(T entity, EntityType entityType) {
        if (entity.getId() != null) {
            checkEntityId(entity.getId(), entityType);
        }
    }

    protected void checkEntityId(Serializable entityId, EntityType entityType) {
        if (entityId == null) {
            throw new BusinessException("ID不能为空");
        }
        validateId(entityId, id -> "Incorrect entityId " + id);
        switch (entityType) {
            case USER:
                checkUserId((Long) entityId);
                return;
            case USER_SETTING:
                checkUserSettingId((Long) entityId);
                return;
            default:
                return;
        }
    }

    protected <E extends HasId<I>, I extends Serializable> E checkEntityId(I entityId, Function<I, E> findingFunction, EntityType entityType) {
        if (entityId == null) {
            return null;
        }
        validateId(entityId, "ID不能为空");
        E entity = findingFunction.apply(entityId);
        Validators.checkNotNull(entity, String.format("%s[%s]不存在", entityType.getName(), entityId));

        return entity;
    }

    protected User checkUserId(Long userId) {
        return checkEntityId(userId, userService::findUserById, EntityType.USER);
    }

    protected Merchant checkMerchantId(Long merchantId) {
        return checkEntityId(merchantId, merchantService::findMerchantById, EntityType.MERCHANT);
    }

    protected UserSetting checkUserSettingId(Long userSettingId) {
        return checkEntityId(userSettingId, userSettingService::findUserSettingById, EntityType.USER_SETTING);
    }


}
