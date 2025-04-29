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
package org.thingsboard.domain.tenant;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.BaseController;
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
	public PageData<TenantProfile> getTenants(PageLink pageLink) {
		return tenantProfileService.findTenantProfiles(pageLink);
	}

}
