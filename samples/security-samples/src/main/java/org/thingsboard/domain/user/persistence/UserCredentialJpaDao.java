package org.thingsboard.domain.user.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.jpa.JpaAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
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
public class UserCredentialJpaDao extends JpaAbstractDao<UserCredentialEntity, UserCredential, Long> implements UserCredentialDao {
	private final UserCredentialRepository repository;

	@Override
	protected Class<UserCredentialEntity> getEntityClass() {
		return UserCredentialEntity.class;
	}

	@Override
	protected JpaRepository<UserCredentialEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public UserCredential findByUserId(Long userId) {
		return DaoUtil.getData(repository.findByUserId(userId));
	}

	@Override
	public UserCredential findByActivateToken(String activateToken) {
		return DaoUtil.getData(repository.findByActivateToken(activateToken));
	}

	@Override
	public UserCredential findByResetToken(String resetToken) {
		return DaoUtil.getData(repository.findByResetToken(resetToken));
	}

	@Override
	@Transactional
	public void removeByUserId(Long userId) {
		repository.deleteByUserId(userId);
	}
}
