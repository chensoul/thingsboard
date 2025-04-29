/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
