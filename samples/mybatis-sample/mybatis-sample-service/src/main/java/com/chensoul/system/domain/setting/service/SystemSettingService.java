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
package com.chensoul.system.domain.setting.service;


import com.chensoul.system.domain.setting.domain.SecuritySetting;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import com.chensoul.system.domain.setting.domain.SystemSettingType;
import javax.servlet.http.HttpServletRequest;

public interface SystemSettingService {

    SystemSetting findSystemSettingByType(String tenantId, SystemSettingType type);

    SystemSetting saveSystemSetting(String tenantId, SystemSetting systemSetting);

    void deleteSystemSettingByTenantIdAndType(String tenantId, SystemSettingType type);

    void deleteSystemSettingByTenantId(String tenantId);

    String getBaseUrl(HttpServletRequest httpServletRequest);

    SecuritySetting getSecuritySetting();
}
