package org.thingsboard.domain.oauth2.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.MybatisAbstractDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.model.OAuth2Domain;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class MybatisOAuth2DomainDao extends MybatisAbstractDao<OAuth2DomainEntity, OAuth2Domain> implements OAuth2DomainDao {
	private final OAuth2DomainMapper mapper;

	@Override
	protected Class<OAuth2DomainEntity> getEntityClass() {
		return OAuth2DomainEntity.class;
	}

	@Override
	protected BaseMapper<OAuth2DomainEntity> getRepository() {
		return mapper;
	}

	@Override
	public List<OAuth2Domain> findByOAuth2ParamId(Long oauth2ParamId) {
		return DaoUtil.convertDataList(mapper.selectList(Wrappers.<OAuth2DomainEntity>lambdaQuery().eq(OAuth2DomainEntity::getOauth2ParamId, oauth2ParamId)));
	}
}
