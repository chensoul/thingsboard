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
package com.chensoul.system.domain.user.service;

import com.chensoul.exception.BusinessException;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.user.mybatis.UserDao;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getUserId;
import com.chensoul.system.user.domain.Authority;
import com.chensoul.system.user.domain.User;
import com.chensoul.validation.DataValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class UserValidator extends DataValidator<User> {
    private final UserDao userDao;
//    private final MerchantDao merchantDao;
//    private final TenantDao tenantDao;

    protected void validateCreate(User user) {
        if (!user.getTenantId().equals(SYS_TENANT_ID)) {
//            validateEntityCountPerTenant(user.getTenantId(), EntityType.USER);
        }
    }

    @Override
    protected User validateUpdate(User user) {
        User old = userDao.findById(user.getId());
        if (old == null) {
            throw new BusinessException("Can't update non existing user!");
        }
        if (!old.getTenantId().equals(user.getTenantId())) {
            throw new BusinessException("Can't update user tenant id!");
        }
        if (!old.getAuthority().equals(user.getAuthority())) {
            throw new BusinessException("Can't update user authority!");
        }
        if (old.getMerchantId() != user.getMerchantId()) {
            throw new BusinessException("Can't update user merchant id!");
        }
        return old;
    }

    @Override
    protected void validateDataImpl(User user) {
        if (StringUtils.isBlank(user.getEmail())) {
            throw new BusinessException("User email should be specified!");
        }

        Authority authority = user.getAuthority();
        if (authority == null) {
            throw new BusinessException("User authority isn't defined!");
        }
        String tenantId = user.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            user.setTenantId(SYS_TENANT_ID);
        }
        Long merchantId = user.getMerchantId();
        if (merchantId == null) {
            user.setMerchantId(null);
        }

        switch (authority) {
            case SYS_ADMIN:
                if (!user.getTenantId().equals(SYS_TENANT_ID)
                    || user.getMerchantId() != null) {
                    throw new BusinessException("System administrator can't be assigned neither to tenant nor to customer!");
                }
                break;
            case TENANT_ADMIN:
                if (user.getTenantId().equals(SYS_TENANT_ID)) {
                    throw new BusinessException("Tenant administrator should be assigned to tenant!");
                } else if (user.getMerchantId() != null) {
                    throw new BusinessException("Tenant administrator can't be assigned to customer!");
                }
                break;
            case MERCHANT_USER:
                if (user.getTenantId().equals(SYS_TENANT_ID)
                    || user.getMerchantId() == null) {
                    throw new BusinessException("Customer user should be assigned to customer!");
                }
                break;
            default:
                break;
        }

        User existentUserWithEmail = userDao.findByEmail(user.getEmail());
        if (existentUserWithEmail != null && !isSameData(existentUserWithEmail, user)) {
            throw new BusinessException("User with email '" + user.getEmail() + "' "
                                        + " already present in database!");
        }
//        if (!user.getTenantId().equals(SYS_TENANT_ID)) {
//            if (!tenantDao.existsById(user.getTenantId())) {
//                throw new BusinessException("User is referencing to non-existent tenant!");
//            }
//        }
//        if (user.getMerchantId() != null) {
//            Merchant merchant = merchantDao.findById(user.getMerchantId());
//            if (merchant == null) {
//                throw new BusinessException("User is referencing to non-existent customer!");
//            } else if (!merchant.getTenantId().equals(tenantId)) {
//                throw new BusinessException("User can't be assigned to customer from different tenant!");
//            }
//        }
    }

    @Override
    public void validateDelete(User user) {
        super.validateDelete(user);

        if (user.getAuthority() == Authority.SYS_ADMIN && getUserId().equals(user.getId())) {
            throw new BusinessException("系统管理员不能删除自己");
        }
    }
}
