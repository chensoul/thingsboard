package org.thingsboard.domain.user.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
import org.thingsboard.domain.user.model.UserCredential;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
@SqlDao
public class MybatisUserCredentialDao extends AbstractMybatisDao<UserCredentialEntity, UserCredential> implements UserCredentialDao {
	private final UserCredentialMapper mapper;

	@Override
	protected Class<UserCredentialEntity> getEntityClass() {
		return UserCredentialEntity.class;
	}

	@Override
	protected BaseMapper<UserCredentialEntity> getRepository() {
		return mapper;
	}

	@Override
	public UserCredential findByUserId(Long userId) {
		return DaoUtil.getData(getRepository().selectOne(Wrappers.<UserCredentialEntity>lambdaQuery().eq(UserCredentialEntity::getUserId, userId)));
	}

	@Override
	public UserCredential findByActivateToken(String activateToken) {
		return DaoUtil.getData(getRepository().selectOne(Wrappers.<UserCredentialEntity>lambdaQuery().eq(UserCredentialEntity::getActivateToken, activateToken)));
	}

	@Override
	public UserCredential findByResetToken(String resetToken) {
		return DaoUtil.getData(getRepository().selectOne(Wrappers.<UserCredentialEntity>lambdaQuery().eq(UserCredentialEntity::getResetToken, resetToken)));
	}

	@Override
	@Transactional
	public void removeByUserId(Long userId) {
		getRepository().delete(Wrappers.<UserCredentialEntity>lambdaQuery().eq(UserCredentialEntity::getUserId, userId));
	}
}
