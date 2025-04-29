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
package com.chensoul.system.domain.tenant.service.impl;

import com.chensoul.data.event.DeleteEntityEvent;
import com.chensoul.data.event.SaveEntityEvent;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.json.JacksonUtils;
import com.chensoul.system.domain.tenant.domain.DefaultTenantProfileConfiguration;
import com.chensoul.system.domain.tenant.domain.Tenant;
import com.chensoul.system.domain.tenant.domain.TenantProfile;
import com.chensoul.system.domain.tenant.domain.TenantProfileData;
import com.chensoul.system.domain.tenant.mybatis.TenantDao;
import com.chensoul.system.domain.tenant.mybatis.TenantProfileDao;
import com.chensoul.system.domain.tenant.service.TenantProfileService;
import com.chensoul.system.domain.tenant.service.TenantProfileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class TenantProfileServiceImpl implements TenantProfileService {
    private final ApplicationEventPublisher eventPublisher;
    private final TenantProfileDao tenantProfileDao;
    private final TenantProfileValidator tenantProfileValidator;
    private final TenantDao tenantDao;

    @Override
    public TenantProfile findOrCreateDefaultTenantProfile() {
        TenantProfile defaultTenantProfile = findDefaultTenantProfile();
        if (defaultTenantProfile == null) {
            defaultTenantProfile = new TenantProfile();
            defaultTenantProfile.setDefaulted(true);
            defaultTenantProfile.setName("Default");
            defaultTenantProfile.setDescription("Default tenant profile");

            TenantProfileData profileData = new TenantProfileData();
            profileData.setConfiguration(new DefaultTenantProfileConfiguration());
            defaultTenantProfile.setExtraBytes(JacksonUtils.toString(profileData).getBytes());

            defaultTenantProfile = saveTenantProfile(defaultTenantProfile);
        }
        return defaultTenantProfile;
    }

    @Override
    public TenantProfile saveTenantProfile(TenantProfile tenantProfile) {
        tenantProfileValidator.validate(tenantProfile);
        TenantProfile savedTenantProfile = tenantProfileDao.save(tenantProfile);

        eventPublisher.publishEvent(SaveEntityEvent.builder().entity(savedTenantProfile)
            .entityId(savedTenantProfile.getId()).created(tenantProfile.getId() == null).build());

        return savedTenantProfile;
    }

    @Override
    public void deleteTenantProfile(TenantProfile tenantProfile) {
        if (tenantProfile != null) {
            tenantProfileDao.removeById(tenantProfile.getId());
            eventPublisher.publishEvent(DeleteEntityEvent.builder().entity(tenantProfile).entityId(tenantProfile.getId()).build());
        }
    }

    @Override
    public TenantProfile findTenantProfileById(Long tenantProfileId) {
        return tenantProfileDao.findById(tenantProfileId);
    }

    @Override
    public TenantProfile findTenantProfileByTenantId(String tenantId) {
        Tenant tenant = tenantDao.findById(tenantId);
        if (tenant == null) {
            return null;
        }
        return tenantProfileDao.findById(tenant.getTenantProfileId());
    }

    @Override
    public TenantProfile findDefaultTenantProfile() {
        return tenantProfileDao.findDefaultTenantProfile();
    }

    @Override
    public TenantProfile setDefaultTenantProfile(TenantProfile tenantProfile) {
        if (tenantProfile != null && !tenantProfile.isDefaulted()) {
            tenantProfile.setDefaulted(true);
            TenantProfile previousDefaultTenantProfile = findDefaultTenantProfile();
            if (previousDefaultTenantProfile == null) {
                saveTenantProfile(tenantProfile);
            } else if (!previousDefaultTenantProfile.getId().equals(tenantProfile.getId())) {
                previousDefaultTenantProfile.setDefaulted(false);
                saveTenantProfile(previousDefaultTenantProfile);
                saveTenantProfile(tenantProfile);
            }
        }
        return tenantProfile;
    }

    @Override
    public PageData<TenantProfile> findTenantProfiles(PageLink pageLink) {
        return tenantProfileDao.findTenantProfiles(pageLink);
    }
}
