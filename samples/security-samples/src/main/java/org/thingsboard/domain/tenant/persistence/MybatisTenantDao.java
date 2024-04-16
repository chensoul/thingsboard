package org.thingsboard.domain.tenant.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.domain.tenant.model.Tenant;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MybatisTenantDao extends MybatisAbstractDao<TenantEntity, Tenant> implements TenantDao {
	private final TenantMapper mapper;

	@Override
	protected Class<TenantEntity> getEntityClass() {
		return TenantEntity.class;
	}

	@Override
	protected BaseMapper<TenantEntity> getRepository() {
		return mapper;
	}

	@Override
	public Page<Tenant> findTenants(Pageable pageable, String textSearch) {
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<TenantEntity> pageResult = getRepository().selectPage(toMybatisPage(pageable),
			Wrappers.<TenantEntity>lambdaQuery().or(StringUtils.isNotBlank(textSearch), i -> i.like(TenantEntity::getEmail, textSearch).or().like(TenantEntity::getName, textSearch)
				.or().like(TenantEntity::getEmail, textSearch)));

		return DaoUtil.toPageData(pageResult);
	}

	@Override
	public Tenant findByName(String name) {
		return DaoUtil.getData(mapper.selectOne(Wrappers.<TenantEntity>lambdaQuery().eq(TenantEntity::getName, name)));
	}
}
