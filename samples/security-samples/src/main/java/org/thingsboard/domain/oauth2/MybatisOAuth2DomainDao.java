package org.thingsboard.domain.oauth2;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Component
public class MybatisOAuth2DomainDao extends MybatisAbstractDao<OAuth2DomainEntity, OAuth2Domain> implements OAuth2DomainDao {
	@Autowired
	private OAuth2DomainMapper mapper;

	@Override
	protected Class<OAuth2DomainEntity> getEntityClass() {
		return OAuth2DomainEntity.class;
	}

	@Override
	protected BaseMapper<OAuth2DomainEntity> getRepository() {
		return mapper;
	}

	@Override
	public List<OAuth2Domain> findByOAuth2ParamId(Long oauth2ParamsId) {
		return DaoUtil.convertDataList(mapper.selectList(Wrappers.<OAuth2DomainEntity>lambdaQuery().eq(OAuth2DomainEntity::getOauth2ParamsId, oauth2ParamsId)));
	}
}
