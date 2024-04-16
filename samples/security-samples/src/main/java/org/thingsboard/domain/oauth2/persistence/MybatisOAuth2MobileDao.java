package org.thingsboard.domain.oauth2.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.domain.oauth2.model.OAuth2Mobile;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class MybatisOAuth2MobileDao extends MybatisAbstractDao<OAuth2MobileEntity, OAuth2Mobile> implements OAuth2MobileDao {
	private final OAuth2MobileMapper mapper;

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
