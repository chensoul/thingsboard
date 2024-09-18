package com.chensoul.system.domain.setting.service.impl;

import com.chensoul.system.domain.setting.domain.SystemSetting;
import com.chensoul.system.domain.setting.mybatis.SystemSettingDao;
import com.chensoul.exception.BusinessException;
import com.chensoul.validation.DataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemSettingValidator extends DataValidator<SystemSetting> {
    private final SystemSettingDao systemSettingDao;

    @Override
    protected void validateCreate(SystemSetting systemSetting) {
        SystemSetting existingSetting = systemSettingDao.findByType(systemSetting.getTenantId(), systemSetting.getType());
        if (existingSetting != null) {
            throw new BusinessException("System setting with such name already exists!");
        }
    }

    @Override
    protected SystemSetting validateUpdate(SystemSetting systemSetting) {
        SystemSetting existentSystemSetting = systemSettingDao.findById(systemSetting.getId());
        if (existentSystemSetting != null) {
            if (!existentSystemSetting.getType().equals(systemSetting.getType())) {
                throw new BusinessException("Changing key of system setting entry is prohibited!");
            }
        }
        return existentSystemSetting;
    }

    @Override
    protected void validateDataImpl(SystemSetting systemSetting) {
        if (systemSetting.getExtra() == null) {
            throw new BusinessException("Json value should be specified!");
        }
    }
}
