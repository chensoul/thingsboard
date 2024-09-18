package com.chensoul.system.domain.merchant.internal.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.mybatis.dao.AbstractDao;
import com.chensoul.system.domain.merchant.Merchant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class MerchantDao extends AbstractDao<MerchantEntity, Merchant, Long> {
  private final MerchantRepository repository;

  @Override
  protected Class<MerchantEntity> getEntityClass() {
    return MerchantEntity.class;
  }

  @Override
  protected BaseMapper<MerchantEntity> getRepository() {
    return repository;
  }

  public Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String name) {
    MerchantEntity entity = repository.findByTenantIdAndName(tenantId, name);
    return Optional.of(DaoUtil.getData(entity));
  }

  public void removeByTenantId(String tenantId) {
    repository.deleteByTenantId(tenantId);
  }

  public PageData<Merchant> findTenants(String tenantId, PageLink pageLink) {
    return DaoUtil.toPageData(repository.findByTenantId(tenantId, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
  }
}
