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
package com.chensoul.system;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface CacheConstants {
    String SESSION_CACHE = "session";
    String SECURITY_SETTING_CACHE = "securitySetting";
    String USER_SETTING_CACHE = "userSetting";
    String USERS_SESSION_INVALIDATION_CACHE = "userSessionsInvalidation";
    String TENANT_PROFILE_CACHE = "tenantProfile";
    String TENANT_CACHE = "tenant";
    String NOTIFICATION_SETTING_CACHE = "notificationSetting";
    String TWO_FA_VERIFICATION_CODE_CACHE = "twoFaVerificationCode";
    String SENT_NOTIFICATIONS_CACHE = "sentNotifications";

    List<String> ALL_CACHES = Collections.unmodifiableList(Arrays.asList(
        SESSION_CACHE,
        SECURITY_SETTING_CACHE,
        USER_SETTING_CACHE,
        USERS_SESSION_INVALIDATION_CACHE,
        TENANT_PROFILE_CACHE,
        TENANT_CACHE,
        NOTIFICATION_SETTING_CACHE,
        TWO_FA_VERIFICATION_CODE_CACHE));
}
