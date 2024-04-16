package org.thingsboard.domain.tenant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.common.event.DeleteEntityEvent;
import org.thingsboard.common.event.SaveEntityEvent;
import org.thingsboard.domain.tenant.model.Tenant;
import org.thingsboard.domain.tenant.model.TenantInfo;
import org.thingsboard.domain.tenant.model.TenantProfile;
import org.thingsboard.domain.tenant.persistence.TenantDao;
import org.thingsboard.domain.tenant.service.TenantProfileService;
import org.thingsboard.domain.tenant.service.TenantService;
import org.thingsboard.domain.tenant.service.TenantValidator;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

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
			TenantProfile tenantProfile = tenantProfileService.findOrCreateDefaultTenantProfile(SYS_TENANT_ID);
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
			tenantDao.removeById(tenant);
			eventPublisher.publishEvent(DeleteEntityEvent.builder().tenantId(tenant.getId())
				.entity(tenant).entityId(tenant.getId()).build());
		}
	}

	@Override
	public Page<Tenant> findTenants(Pageable pageable, String textSearch) {
		return tenantDao.findTenants(pageable, textSearch);
	}
}
