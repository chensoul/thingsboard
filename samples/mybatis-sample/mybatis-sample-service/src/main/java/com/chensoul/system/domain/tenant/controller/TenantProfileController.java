package com.chensoul.system.domain.tenant.controller;

import com.chensoul.system.domain.tenant.domain.TenantProfile;
import com.chensoul.system.domain.tenant.service.TenantProfileService;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import javax.validation.Valid;
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
public class TenantProfileController {
    private final TenantProfileService tenantProfileService;

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenantProfile/{tenantProfileId}", method = RequestMethod.GET)
    public TenantProfile getTenantProfileById(@PathVariable("tenantProfileId") Long tenantProfileId) {
        return tenantProfileService.findTenantProfileById(tenantProfileId);
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenantProfile", method = RequestMethod.POST)
    public TenantProfile saveTenantProfile(@RequestBody @Valid TenantProfile tenantProfile) throws Exception {
        return tenantProfileService.saveTenantProfile(tenantProfile);
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenantProfile/{tenantProfileId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTenantProfile(@PathVariable("tenantProfileId") Long tenantProfileId) throws Exception {
        TenantProfile old = tenantProfileService.findTenantProfileById(tenantProfileId);
        tenantProfileService.deleteTenantProfile(old);
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenantProfile/{tenantProfileId}/default", method = RequestMethod.POST)
    public TenantProfile setDefaultTenantProfile(@PathVariable("tenantProfileId") Long tenantProfileId) {
        TenantProfile tenantProfile = tenantProfileService.findTenantProfileById(tenantProfileId);
        return tenantProfileService.setDefaultTenantProfile(tenantProfile);
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenantProfiles", method = RequestMethod.GET)
    public PageData<TenantProfile> getTenants(PageLink pageLink) {
        return tenantProfileService.findTenantProfiles(pageLink);
    }

}
