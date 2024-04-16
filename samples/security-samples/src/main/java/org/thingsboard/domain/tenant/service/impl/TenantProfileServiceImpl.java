package org.thingsboard.domain.tenant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.common.event.DeleteEntityEvent;
import org.thingsboard.common.event.SaveEntityEvent;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.tenant.model.DefaultTenantProfileConfiguration;
import org.thingsboard.domain.tenant.model.TenantProfile;
import org.thingsboard.domain.tenant.model.TenantProfileData;
import org.thingsboard.domain.tenant.persistence.TenantProfileDao;
import org.thingsboard.domain.tenant.service.TenantProfileService;
import org.thingsboard.domain.tenant.service.TenantProfileValidator;

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

	@Override
	public TenantProfile findOrCreateDefaultTenantProfile(String tenantId) {
		TenantProfile defaultTenantProfile = findDefaultTenantProfile();
		if (defaultTenantProfile == null) {
			defaultTenantProfile = new TenantProfile();
			defaultTenantProfile.setIsDefault(true);
			defaultTenantProfile.setName("Default");
			defaultTenantProfile.setDescription("Default tenant profile");
			defaultTenantProfile.setTenantId(tenantId);

			TenantProfileData profileData = new TenantProfileData();
			profileData.setConfiguration(new DefaultTenantProfileConfiguration());
			defaultTenantProfile.setExtraBytes(JacksonUtil.toString(profileData).getBytes());

			defaultTenantProfile = saveTenantProfile(tenantId, defaultTenantProfile);
		}
		return defaultTenantProfile;
	}

	@Override
	public TenantProfile saveTenantProfile(String tenantId, TenantProfile tenantProfile) {
		tenantProfileValidator.validate(tenantProfile);
		TenantProfile savedTenantProfile = tenantProfileDao.save(tenantProfile);

		eventPublisher.publishEvent(SaveEntityEvent.builder().entity(savedTenantProfile)
			.entityId(savedTenantProfile.getId()).created(tenantProfile.getId() == null).build());

		return savedTenantProfile;
	}

	@Override
	public void deleteTenantProfile(TenantProfile tenantProfileId) {
		if (tenantProfileId != null) {
			tenantProfileDao.removeById(tenantProfileId);
			eventPublisher.publishEvent(DeleteEntityEvent.builder().tenantId(tenantProfileId.getTenantId()).entity(tenantProfileId).entityId(tenantProfileId).build());
		}
	}

	@Override
	public TenantProfile findTenantProfileById(Long tenantProfileId) {
		return tenantProfileDao.findById(tenantProfileId);
	}

	@Override
	public TenantProfile findDefaultTenantProfile() {
		return tenantProfileDao.findDefaultTenantProfile();
	}

	@Override
	public TenantProfile setDefaultTenantProfile(TenantProfile tenantProfile) {
		String tenantId = tenantProfile.getTenantId();

		if (tenantProfile != null && !tenantProfile.getIsDefault()) {
			tenantProfile.setIsDefault(true);
			TenantProfile previousDefaultTenantProfile = findDefaultTenantProfile();
			if (previousDefaultTenantProfile == null) {
				saveTenantProfile(tenantId, tenantProfile);
			} else if (!previousDefaultTenantProfile.getId().equals(tenantProfile.getId())) {
				previousDefaultTenantProfile.setIsDefault(false);
				saveTenantProfile(tenantId, previousDefaultTenantProfile);
				saveTenantProfile(tenantId, tenantProfile);
			}
		}
		return tenantProfile;
	}

	@Override
	public Page<TenantProfile> findTenantProfiles(Pageable pageable, String tenantId, String textSearch) {
		return tenantProfileDao.findTenantProfiles(pageable, tenantId, textSearch);
	}
}
