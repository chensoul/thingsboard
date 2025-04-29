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
package com.chensoul.system.infrastructure.security.util;


import com.chensoul.exception.BusinessException;
import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import static com.chensoul.system.ExceptionConstants.YOU_AREN_T_AUTHORIZED_TO_PERFORM_THIS_OPERATION;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class SecurityUtils {
    public static SecurityUser getCurrentUser() {
        SecurityUser securityUser = getSecurityUser();
        if (securityUser == null) {
            throw new BusinessException(YOU_AREN_T_AUTHORIZED_TO_PERFORM_THIS_OPERATION);
        }
        return securityUser;
    }

    public static SecurityUser getSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            return (SecurityUser) authentication.getPrincipal();
        }
        return null;
    }

    public static String getTenantId() {
        return getCurrentUser().getTenantId();
    }

    public static Long getUserId() {
        return getCurrentUser().getId();
    }

    public static boolean isSysTenantId(String tenantId) {
        return tenantId == null || tenantId.equals(SYS_TENANT_ID);
    }

}
