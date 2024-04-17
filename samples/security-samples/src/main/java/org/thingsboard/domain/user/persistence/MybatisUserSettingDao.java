package org.thingsboard.domain.user.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.domain.user.model.UserSetting;
import org.thingsboard.domain.user.model.UserSettingType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
@SqlDao
public class MybatisUserSettingDao extends AbstractMybatisDao<UserSettingEntity, UserSetting> implements UserSettingDao {
	private final UserSettingMapper mapper;

	@Override
	protected Class<UserSettingEntity> getEntityClass() {
		return UserSettingEntity.class;
	}

	@Override
	protected BaseMapper<UserSettingEntity> getRepository() {
		return mapper;
	}

	@Override
	public UserSetting findByUserIdAndType(Long userId, UserSettingType type) {
		UserSettingEntity entity = getRepository().selectOne(Wrappers.<UserSettingEntity>lambdaQuery().eq(UserSettingEntity::getUserId, userId).eq(UserSettingEntity::getType, type.name().toLowerCase()));
		return DaoUtil.getData(entity);
	}

	@Override
	public void removeByUserId(Long userId) {
		getRepository().delete(Wrappers.<UserSettingEntity>lambdaQuery().eq(UserSettingEntity::getUserId, userId));
	}
}
