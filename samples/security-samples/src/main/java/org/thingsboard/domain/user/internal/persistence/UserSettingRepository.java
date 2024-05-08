package org.thingsboard.domain.user.internal.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.domain.user.UserSettingType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface UserSettingRepository extends JpaRepository<UserSettingEntity, Long> {
	@Query(value = "SELECT * FROM user_settings WHERE type = :type AND (extra #> :path) IS NOT NULL", nativeQuery = true)
	List<UserSettingEntity> findByTypeAndPathExisting(@Param("type") UserSettingType type, @Param("path") String[] path);

	UserSettingEntity findByUserIdAndType(Long userId, UserSettingType type);

	void deleteByUserId(Long userId);
}
