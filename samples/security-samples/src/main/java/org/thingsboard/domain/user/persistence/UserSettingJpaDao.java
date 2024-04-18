package org.thingsboard.domain.user.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.JpaAbstractDao;
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
public class UserSettingJpaDao extends JpaAbstractDao<UserSettingEntity, UserSetting, Long> implements UserSettingDao {
	private final UserSettingRepository repository;

	@Override
	protected Class<UserSettingEntity> getEntityClass() {
		return UserSettingEntity.class;
	}

	@Override
	protected JpaRepository<UserSettingEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public UserSetting findByUserIdAndType(Long userId, UserSettingType type) {
		UserSettingEntity entity = repository.findByUserIdAndType(userId, type);
		return DaoUtil.getData(entity);
	}

	@Override
	public void removeByUserId(Long userId) {
		repository.deleteByUserId(userId);
	}
}
