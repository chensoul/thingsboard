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
package org.thingsboard.domain.tenant.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.event.DeleteEntityEvent;
import org.thingsboard.common.model.event.SaveEntityEvent;
import org.thingsboard.domain.tenant.Tenant;
import org.thingsboard.domain.tenant.TenantInfo;
import org.thingsboard.domain.tenant.TenantProfile;
import org.thingsboard.domain.tenant.internal.persistence.TenantDao;
import org.thingsboard.domain.tenant.TenantProfileService;
import org.thingsboard.domain.tenant.TenantService;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class TenantServiceImpl implements TenantService {
	private final TenantProfileService tenantProfileService;
	private final ApplicationEventPublisher eventPublisher;
	private final TenantDao tenantDao;
	private final TenantValidator tenantValidator;

	@Override
	public Tenant findTenantById(String tenantId) {
		return tenantDao.findById(tenantId);
	}

	@Override
	public Tenant findTenantByName(String name) {
		return tenantDao.findByName(name);
	}

	@Override
	public TenantInfo findTenantInfoById(String tenantId) {
		Tenant data = findTenantById(tenantId);

		TenantInfo tenantInfo = new TenantInfo();
		if (data == null) {
			return tenantInfo;
		}

		BeanUtils.copyProperties(data, tenantInfo);

		if (data.getTenantProfileId() == null) {
			TenantProfile tenantProfile = this.tenantProfileService.findTenantProfileById(data.getTenantProfileId());
			tenantInfo.setTenantProfileName(tenantProfile.getName());
		}
		return tenantInfo;
	}

	@Override
	public Tenant saveTenant(Tenant tenant) {
		if (tenant.getTenantProfileId() == null) {
			TenantProfile tenantProfile = tenantProfileService.findOrCreateDefaultTenantProfile();
			tenant.setTenantProfileId(tenantProfile.getId());
		}

		tenantValidator.validate(tenant);
		boolean create = tenant.getId() == null;
		Tenant savedTenant = tenantDao.save(tenant);

		eventPublisher.publishEvent(SaveEntityEvent.builder()
			.entityId(savedTenant.getId()).entity(savedTenant).created(create).build());

		return savedTenant;
	}

	@Override
	public boolean tenantExists(String tenantId) {
		return tenantDao.existsById(tenantId);
	}

	@Override
	public void deleteTenant(Tenant tenant) {
		if (tenant != null) {
			tenantDao.removeById(tenant.getTenantId());
			eventPublisher.publishEvent(DeleteEntityEvent.builder().tenantId(tenant.getId())
				.entity(tenant).entityId(tenant.getId()).build());
		}
	}

	@Override
	public Page<Tenant> findTenants(Pageable pageable, String textSearch) {
		return tenantDao.findTenants(pageable, textSearch);
	}
}
