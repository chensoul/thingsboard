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
package com.chensoul.system.domain.tenant.controller;

import static com.chensoul.system.ControllerConstants.TENANT_ID;
import com.chensoul.system.domain.tenant.domain.Tenant;
import com.chensoul.system.domain.tenant.domain.TenantInfo;
import com.chensoul.system.domain.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TenantController {
    private final TenantService tenantService;

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @GetMapping(value = "/tenants")
    public Page<Tenant> getTenants(Pageable pageable, @RequestParam(required = false, defaultValue = "") String textSearch) {
        return tenantService.findTenants(pageable, textSearch);
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/tenant/{tenantId}")
    public Tenant getTenantById(@PathVariable(TENANT_ID) String tenantId) {
        return tenantService.findTenantById(tenantId);
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @GetMapping(value = "/tenant/info/{tenantId}")
    public TenantInfo getTenantInfoById(@PathVariable(TENANT_ID) String tenantId) {
        return tenantService.findTenantInfoById(tenantId);
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @PostMapping(value = "/tenant")
    public Tenant saveTenant(@RequestBody Tenant tenant) throws Exception {
        return tenantService.saveTenant(tenant);
    }

    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @DeleteMapping(value = "/tenant/{tenantId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTenant(@PathVariable(TENANT_ID) String tenantId) throws Exception {
        Tenant tenant = tenantService.findTenantById(tenantId);
        tenantService.deleteTenant(tenant);
    }
}
