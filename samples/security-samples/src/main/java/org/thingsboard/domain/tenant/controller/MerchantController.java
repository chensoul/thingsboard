package org.thingsboard.domain.tenant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import static org.thingsboard.common.ControllerConstants.MERCHANT_ID;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.service.BaseController;
import org.thingsboard.domain.tenant.model.Merchant;
import static org.thingsboard.server.security.SecurityUtils.getTenantId;
import org.thingsboard.server.security.permission.Operation;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MerchantController extends BaseController {
	@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'MERCHANT_USER')")
	@RequestMapping(value = "/merchant/{merchantId}", method = RequestMethod.GET)
	public Merchant getMerchantById(@PathVariable(MERCHANT_ID) Long merchantId) {
		return checkMerchantId(merchantId, Operation.GET);
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/merchant", method = RequestMethod.POST)
	public Merchant saveMerchant(@RequestBody Merchant merchant) throws Exception {
		merchant.setTenantId(getTenantId());
		Merchant old = checkMerchantId(merchant.getId(), Operation.POST);
		return doSaveAndLog(merchant, old, EntityType.MERCHANT, (t) -> merchantService.saveMerchant(merchant));
	}

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/merchant/{merchantId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteMerchant(@PathVariable(MERCHANT_ID) Long merchantId) throws Exception {
		Merchant old = checkMerchantId(merchantId, Operation.DELETE);
		doDeleteAndLog(old, EntityType.USER, (t) -> merchantService.deleteMerchant(old));
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/merchants", method = RequestMethod.GET)
	public Page<Merchant> getTenants(Pageable pageable, @RequestParam(required = false) String textSearch) {
		return merchantService.findTenants(pageable, getTenantId(), textSearch);
	}
}
