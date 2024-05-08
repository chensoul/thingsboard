package org.thingsboard.domain.merchant.internal;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.common.model.event.DeleteEntityEvent;
import org.thingsboard.common.model.event.SaveEntityEvent;
import org.thingsboard.domain.merchant.Merchant;
import org.thingsboard.domain.merchant.internal.persistence.MerchantDao;
import org.thingsboard.domain.merchant.MerchantService;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Service
public class MerchantServiceImpl implements MerchantService {
	private final ApplicationEventPublisher eventPublisher;
	private final MerchantDao merchantDao;
	private final MerchantValidator merchantValidator;

	@Override
	public Merchant findMerchantById(Long merchantId) {
		return merchantDao.findById(merchantId);
	}

	@Override
	public Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String customerName) {
		return merchantDao.findMerchantByTenantIdAndName(tenantId, customerName);
	}

	@Override
	public Merchant saveMerchant(Merchant merchant) {
		merchantValidator.validate(merchant);
		Merchant save = merchantDao.save(merchant);

		eventPublisher.publishEvent(SaveEntityEvent.builder()
			.entityId(save.getId()).entity(save).created(true).build());
		return save;
	}

	@Override
	public void deleteMerchant(Merchant merchant) {
		if (merchant != null) {
			merchantDao.removeById(merchant.getId());
			eventPublisher.publishEvent(DeleteEntityEvent.builder().tenantId(merchant.getTenantId())
				.entity(merchant).entityId(merchant.getId()).build());
		}
	}

	@Override
	public void deleteMerchantByTenantId(String tenantId) {
		merchantDao.removeByTenantId(tenantId);
	}

	@Override
	public PageData<Merchant> findTenant(String tenantId, PageLink pageLink) {
		return merchantDao.findTenants( tenantId, pageLink);
	}
}
