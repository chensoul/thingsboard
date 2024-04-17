package org.thingsboard.domain.tenant;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.service.BaseController;
import org.thingsboard.domain.tenant.model.TenantProfile;
import static org.thingsboard.server.security.SecurityUtils.getTenantId;
import org.thingsboard.server.security.permission.Operation;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Validated
@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TenantProfileController extends BaseController {
	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/tenantProfile/{tenantProfileId}", method = RequestMethod.GET)
	public TenantProfile getTenantProfileById(@PathVariable("tenantProfileId") Long tenantProfileId) {
		return checkTenantProfileId(tenantProfileId, Operation.READ);
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/tenantProfile", method = RequestMethod.POST)
	public TenantProfile saveTenantProfile(@RequestBody @Valid TenantProfile tenantProfile) throws Exception {
		TenantProfile old = checkTenantProfileId(tenantProfile.getId(), Operation.WRITE);
		return doSaveAndLog(tenantProfile, old, EntityType.MERCHANT, (t) -> tenantProfileService.saveTenantProfile(tenantProfile));
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/tenantProfile/{tenantProfileId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteTenantProfile(@PathVariable("tenantProfileId") Long tenantProfileId) throws Exception {
		TenantProfile old = checkTenantProfileId(tenantProfileId, Operation.DELETE);
		doDeleteAndLog(old, EntityType.TENANT_PROFILE, (t) -> tenantProfileService.deleteTenantProfile(old));
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/tenantProfile/{tenantProfileId}/default", method = RequestMethod.POST)
	public TenantProfile setDefaultTenantProfile(@PathVariable("tenantProfileId") Long tenantProfileId) {
		TenantProfile tenantProfile = checkTenantProfileId(tenantProfileId, Operation.WRITE);
		return tenantProfileService.setDefaultTenantProfile(tenantProfile);
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/tenantProfiles", method = RequestMethod.GET)
	public Page<TenantProfile> getTenants(Pageable pageable, @RequestParam(required = false) String textSearch) {
		return tenantProfileService.findTenantProfiles(pageable, getTenantId(), textSearch);
	}

}
