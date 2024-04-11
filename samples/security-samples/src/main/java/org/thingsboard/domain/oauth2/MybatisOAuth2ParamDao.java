package org.thingsboard.domain.oauth2;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.MybatisAbstractDao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Component
public class MybatisOAuth2ParamDao extends MybatisAbstractDao<OAuth2ParamEntity, OAuth2Param> implements OAuth2ParamDao {
	@Autowired
	private OAuth2ParamMapper mapper;

	@Override
	protected Class<OAuth2ParamEntity> getEntityClass() {
		return OAuth2ParamEntity.class;
	}

	@Override
	protected BaseMapper<OAuth2ParamEntity> getRepository() {
		return mapper;
	}

	@Override
	public void deleteAll() {

	}

	@Override
	public List<OAuth2Param> findByTenantId(String tenantId) {
		return List.of();
	}
}
