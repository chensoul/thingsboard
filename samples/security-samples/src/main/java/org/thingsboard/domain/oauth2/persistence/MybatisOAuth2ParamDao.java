package org.thingsboard.domain.oauth2.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.model.OAuth2Param;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@Component
@RequiredArgsConstructor
public class MybatisOAuth2ParamDao extends AbstractMybatisDao<OAuth2ParamEntity, OAuth2Param> implements OAuth2ParamDao {
	private final OAuth2ParamMapper mapper;

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
		mapper.delete(Wrappers.emptyWrapper());
	}

	@Override
	public List<OAuth2Param> findByTenantId(String tenantId) {
		return DaoUtil.convertDataList(mapper.selectList(Wrappers.<OAuth2ParamEntity>lambdaQuery().eq(OAuth2ParamEntity::getTenantId, tenantId)));
	}
}
