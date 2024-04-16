package org.thingsboard.domain.oauth2.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.thingsboard.domain.oauth2.model.SchemeType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Mapper
public interface OAuth2RegistrationMapper extends BaseMapper<OAuth2RegistrationEntity> {

	@Select("<script>SELECT reg.* " +
			"FROM oauth2_registration reg " +
			"LEFT JOIN oauth2_param param on reg.oauth2_param_id = param.id " +
			"LEFT JOIN oauth2_domain domain on reg.oauth2_param_id = domain.oauth2_param_id " +
			"WHERE param.enabled = true " +
			"AND domain.domain_name = #{domainName} " +
			"AND domain.domain_scheme IN <foreach collection='domainSchemes' item='it' open='(' separator=',' close=')'> #{it} </foreach>"+
			"<if test=\"pkgName != null\">" +
			"AND EXISTS (SELECT mobile FROM oauth2_mobile mobile WHERE mobile.oauth2_param_id = reg.oauth2_param_id AND mobile.pkg_name = #{pkgName}) " +
			"</if>" +
			"<if test=\"platformFilter != null\">" +
			"AND reg.platforms LIKE #{platformFilter}" +
			"</if></script>")
	List<OAuth2RegistrationEntity> findEnabledByDomainSchemesDomainNameAndPkgNameAndPlatformType(@Param("domainSchemes") List<SchemeType> domainSchemes,
																								 @Param("domainName") String domainName,
																								 @Param("pkgName") String pkgName,
																								 @Param("platformFilter") String platformFilter);
}
