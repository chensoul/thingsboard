package com.chensoul.system.domain.tenant.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chensoul.system.domain.tenant.domain.TenantProfile;
import com.chensoul.data.dao.DaoUtil;
import com.chensoul.data.model.page.PageData;
import com.chensoul.data.model.page.PageLink;
import com.chensoul.mybatis.dao.AbstractDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
//@SqlDao
@RequiredArgsConstructor
@Component
public class TenantProfileDao extends AbstractDao<TenantProfileEntity, TenantProfile, Long> {
  private final TenantProfileMapper mapper;

  @Override
  protected Class<TenantProfileEntity> getEntityClass() {
    return TenantProfileEntity.class;
  }

  @Override
  protected BaseMapper<TenantProfileEntity> getRepository() {
    return mapper;
  }

  public TenantProfile findDefaultTenantProfile() {
    return DaoUtil.getData(mapper.selectOne(Wrappers.<TenantProfileEntity>lambdaQuery().eq(TenantProfileEntity::isDefaulted, true)));
  }

  public PageData<TenantProfile> findTenantProfiles(PageLink pageLink) {
    return null;//DaoUtil.toPageData(mapper.findTenantProfiles(pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
  }
}
