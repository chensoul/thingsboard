package org.thingsboard.domain.user.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
import org.thingsboard.domain.user.model.Authority;
import org.thingsboard.domain.user.model.User;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class MybatisUserDao extends AbstractMybatisDao<UserEntity, User> implements UserDao {
	private final UserMapper mapper;

	@Override
	protected Class<UserEntity> getEntityClass() {
		return UserEntity.class;
	}

	@Override
	protected BaseMapper<UserEntity> getRepository() {
		return mapper;
	}

	@Override
	public User findByEmail(String email) {
		return DaoUtil.getData(mapper.selectOne(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getEmail, email)));
	}

	@Override
	public Page<User> findTenantAndCustomerUsers(Pageable pageable, Set<String> tenantIds, Set<Long> merchantIds, Set<Long> userIds, Authority authority, String textSearch) {
		com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserEntity> pageResult = mapper.selectPage(toMybatisPage(pageable),
			Wrappers.<UserEntity>lambdaQuery()
				.in(CollectionUtils.isNotEmpty(tenantIds), UserEntity::getTenantId, tenantIds)
				.in(CollectionUtils.isNotEmpty(merchantIds), UserEntity::getMerchantId, merchantIds)
				.in(CollectionUtils.isNotEmpty(userIds), UserEntity::getId, userIds)
				.eq(authority != null, UserEntity::getAuthority, authority)
				.or(StringUtils.isNotBlank(textSearch), i -> i.like(UserEntity::getEmail, textSearch).or().like(UserEntity::getName, textSearch)
					.or().like(UserEntity::getEmail, textSearch)));

		return DaoUtil.toPageData(pageResult);
	}

	@Override
	public Long countByTenantId(String tenantId) {
		return mapper.selectCount(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getTenantId, tenantId));
	}
}
