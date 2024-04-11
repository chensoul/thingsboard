package org.thingsboard.domain.user.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.aspect.SqlDao;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.domain.user.model.UserSetting;
import org.thingsboard.domain.user.model.UserSettingType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@AllArgsConstructor
@Component
@SqlDao
public class MybatisUserSettingDao extends MybatisAbstractDao<UserSettingEntity, UserSetting> implements UserSettingDao {
	private UserSettingMapper mapper;

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
}
