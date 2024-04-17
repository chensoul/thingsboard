package org.thingsboard.domain.tenant.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
import org.thingsboard.domain.tenant.model.Merchant;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MybatisMerchantDao extends AbstractMybatisDao<MerchantEntity, Merchant> implements MerchantDao {
	private final MerchantMapper mapper;

	@Override
	protected Class<MerchantEntity> getEntityClass() {
		return MerchantEntity.class;
	}

	@Override
	protected BaseMapper<MerchantEntity> getRepository() {
		return mapper;
	}

	@Override
	public Optional<Merchant> findMerchantByTenantIdAndName(String tenantId, String name) {
		MerchantEntity entity = getRepository().selectOne(Wrappers.<MerchantEntity>lambdaQuery().eq(MerchantEntity::getTenantId, tenantId).eq(MerchantEntity::getName, name));
		return Optional.of(DaoUtil.getData(entity));
	}

	@Override
	public void removeByTenantId(String tenantId) {
		getRepository().delete(Wrappers.<MerchantEntity>lambdaQuery().eq(MerchantEntity::getTenantId, tenantId));
	}

	@Override
	public Page<Merchant> findTenants(Pageable pageable, String tenantId, String textSearch) {
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<MerchantEntity> pageResult = getRepository().selectPage(toMybatisPage(pageable),
			Wrappers.<MerchantEntity>lambdaQuery()
				.eq(StringUtils.isNotEmpty(tenantId), MerchantEntity::getTenantId, tenantId)
				.or(StringUtils.isNotBlank(textSearch), i -> i.like(MerchantEntity::getEmail, textSearch).or().like(MerchantEntity::getName, textSearch).or().like(MerchantEntity::getEmail, textSearch)));

		return new PageImpl<>(pageResult.getRecords().stream().map(DaoUtil::getData).collect(Collectors.toList()));
	}
}
