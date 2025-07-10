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
package com.chensoul.system.domain.setting.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import com.chensoul.system.domain.setting.domain.SystemSettingType;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.mybatis.dao.AbstractDao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@AllArgsConstructor
@Component
public class SystemSettingDao extends AbstractDao<SystemSettingEntity, SystemSetting, Long> {
    private SystemSettingMapper mapper;

    @Override
    protected Class<SystemSettingEntity> getEntityClass() {
        return SystemSettingEntity.class;
    }

    @Override
    protected BaseMapper<SystemSettingEntity> getRepository() {
        return mapper;
    }

    public SystemSetting findByType(String tenantId, SystemSettingType type) {
        return DaoUtil.getData(mapper.selectOne(Wrappers.<SystemSettingEntity>lambdaQuery().eq(SystemSettingEntity::getTenantId, tenantId).eq(SystemSettingEntity::getType, type)));
    }

    public void removeByTenantIdAndType(String tenantId, SystemSettingType type) {
        mapper.delete(Wrappers.<SystemSettingEntity>lambdaQuery().eq(SystemSettingEntity::getTenantId, tenantId).eq(SystemSettingEntity::getType, type));
    }

    public void removeByTenantId(String tenantId) {
        mapper.delete(Wrappers.<SystemSettingEntity>lambdaQuery().eq(SystemSettingEntity::getTenantId, tenantId));
    }
}
