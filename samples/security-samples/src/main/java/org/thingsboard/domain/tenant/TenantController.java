package org.thingsboard.domain.tenant;

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
import static org.thingsboard.common.ControllerConstants.TENANT_ID;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.BaseController;
import org.thingsboard.domain.tenant.model.Tenant;
import org.thingsboard.domain.tenant.model.TenantInfo;
import org.thingsboard.server.security.permission.Operation;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TenantController extends BaseController {
	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@GetMapping(value = "/tenants")
	public Page<Tenant> getTenants(Pageable pageable, @RequestParam(required = false, defaultValue = "") String textSearch) {
		return tenantService.findTenants(pageable, textSearch);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@GetMapping(value = "/tenant/{tenantId}")
	public Tenant getTenantById(@PathVariable(TENANT_ID) String tenantId) {
		return checkTenantId(tenantId, Operation.READ);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
	@GetMapping(value = "/tenant/info/{tenantId}")
	public TenantInfo getTenantInfoById(@PathVariable(TENANT_ID) String tenantId) {
		return checkTenantInfoId(tenantId, Operation.READ);
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@PostMapping(value = "/tenant")
	public Tenant saveTenant(@RequestBody Tenant tenant) throws Exception {
		Tenant old = checkTenantId(tenant.getId(), Operation.WRITE);
		return doSaveAndLog(tenant, old, EntityType.TENANT, (t) -> tenantService.saveTenant(tenant));
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@DeleteMapping(value = "/tenant/{tenantId}")
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteTenant(@PathVariable(TENANT_ID) String tenantId) throws Exception {
		Tenant old = checkTenantId(tenantId, Operation.DELETE);
		doDeleteAndLog(old, EntityType.USER, (t) -> tenantService.deleteTenant(old));
	}
}
