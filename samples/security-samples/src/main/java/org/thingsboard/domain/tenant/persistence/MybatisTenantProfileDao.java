package org.thingsboard.domain.tenant.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.domain.tenant.model.TenantProfile;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MybatisTenantProfileDao extends MybatisAbstractDao<TenantProfileEntity, TenantProfile> implements TenantProfileDao {
	private final TenantProfileMapper mapper;

	@Override
	protected Class<TenantProfileEntity> getEntityClass() {
		return TenantProfileEntity.class;
	}

	@Override
	protected BaseMapper<TenantProfileEntity> getRepository() {
		return mapper;
	}

	@Override
	public TenantProfile findDefaultTenantProfile() {
		TenantProfileEntity entity = getRepository().selectOne(Wrappers.<TenantProfileEntity>lambdaQuery()
			.eq(TenantProfileEntity::getIsDefault, true));

		return DaoUtil.getData(entity);
	}

	@Override
	public Page<TenantProfile> findTenantProfiles(Pageable pageable, String tenantId, String textSearch) {
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<TenantProfileEntity> pageResult = getRepository().selectPage(toMybatisPage(pageable),
			Wrappers.<TenantProfileEntity>lambdaQuery()
				.eq(StringUtils.isNotEmpty(tenantId), TenantProfileEntity::getTenantId, tenantId)
				.or(StringUtils.isNotBlank(textSearch), i -> i.like(TenantProfileEntity::getName, textSearch).or()
				.like(TenantProfileEntity::getDescription, textSearch)));

		return new PageImpl<>(pageResult.getRecords().stream().map(DaoUtil::getData).collect(Collectors.toList()));
	}
}
