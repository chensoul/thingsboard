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
package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.domain.oauth2.SchemeType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface OAuth2RegistrationRepository extends JpaRepository<OAuth2RegistrationEntity, String> {

	@Query("SELECT reg " +
		   "FROM OAuth2RegistrationEntity reg " +
		   "LEFT JOIN OAuth2ParamEntity param on reg.oauth2ParamId = param.id " +
		   "LEFT JOIN OAuth2DomainEntity domain on reg.oauth2ParamId = domain.oauth2ParamId " +
		   "WHERE param.enabled = true " +
		   "AND domain.domainName = :domainName " +
		   "AND domain.domainScheme IN (:domainSchemes) " +
		   "AND (:pkgName IS NULL OR EXISTS (SELECT mobile FROM OAuth2MobileEntity mobile WHERE mobile.oauth2ParamId = reg.oauth2ParamId AND mobile.pkgName = :pkgName)) " +
		   "AND (:platformFilter IS NULL OR reg.platforms IS NULL OR reg.platforms = '' OR reg.platforms LIKE :platformFilter)")

	List<OAuth2RegistrationEntity> findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(@Param("domainSchemes") List<SchemeType> domainSchemes,
																								 @Param("domainName") String domainName,
																								 @Param("pkgName") String pkgName,
																								 @Param("platformFilter") String platformFilter);

	List<OAuth2RegistrationEntity> findByOauth2ParamId(Long oauth2ParamId);

	@Query("SELECT mobile.appSecret " +
		   "FROM OAuth2MobileEntity mobile " +
		   "LEFT JOIN OAuth2RegistrationEntity reg on mobile.oauth2ParamId = reg.oauth2ParamId " +
		   "WHERE reg.id = :registrationId " +
		   "AND mobile.pkgName = :pkgName")
	String findAppSecret(@Param("registrationId") String id,
						 @Param("pkgName") String pkgName);

}
