package org.thingsboard.domain.user.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.aspect.SqlDao;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.domain.user.model.UserCredential;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@AllArgsConstructor
@Component
@SqlDao
public class MybatisUserCredentialDao extends MybatisAbstractDao<UserCredentialEntity, UserCredential> implements UserCredentialDao {
	private UserCredentialMapper mapper;

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
	public void removeByUserId(Long userId) {
		getRepository().delete(Wrappers.<UserCredentialEntity>lambdaQuery().eq(UserCredentialEntity::getUserId, userId));
	}
}
