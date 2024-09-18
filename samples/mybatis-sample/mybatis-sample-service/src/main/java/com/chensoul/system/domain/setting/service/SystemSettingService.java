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
