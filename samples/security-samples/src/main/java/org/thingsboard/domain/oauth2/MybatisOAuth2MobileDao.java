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
public class MybatisOAuth2MobileDao extends MybatisAbstractDao<OAuth2MobileEntity, OAuth2Mobile> implements OAuth2MobileDao {
	@Autowired
	private OAuth2MobileMapper mapper;

	@Override
	protected Class<OAuth2MobileEntity> getEntityClass() {
		return OAuth2MobileEntity.class;
	}

	@Override
	protected BaseMapper<OAuth2MobileEntity> getRepository() {
		return mapper;
	}

	@Override
	public List<OAuth2Mobile> findByOAuth2ParamId(Long oauth2ParamId) {
		return List.of();
	}
}
