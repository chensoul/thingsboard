package org.thingsboard.domain.oauth2.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.common.dao.aspect.SqlDao;
import org.thingsboard.common.dao.DaoUtil;
import org.thingsboard.common.dao.mybatis.AbstractMybatisDao;
import org.thingsboard.domain.oauth2.model.OAuth2ClientRegistrationTemplate;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class MybatisOAuth2ClientRegistrationTemplateDao extends AbstractMybatisDao<OAuth2ClientRegistrationTemplateEntity, OAuth2ClientRegistrationTemplate> implements OAuth2ClientRegistrationTemplateDao {
	private final OAuth2ClientRegistrationTemplateMapper mapper;

	@Override
	protected Class<OAuth2ClientRegistrationTemplateEntity> getEntityClass() {
		return OAuth2ClientRegistrationTemplateEntity.class;
	}

	@Override
	protected BaseMapper<OAuth2ClientRegistrationTemplateEntity> getRepository() {
		return mapper;
	}

	@Override
	public OAuth2ClientRegistrationTemplate findByProviderId(String providerId) {
		return DaoUtil.getData(mapper.selectOne(Wrappers.<OAuth2ClientRegistrationTemplateEntity>lambdaQuery().eq(OAuth2ClientRegistrationTemplateEntity::getProviderId, providerId)));
	}

	@Override
	public List<OAuth2ClientRegistrationTemplate> findAll() {
		return DaoUtil.convertDataList(mapper.selectList(Wrappers.emptyWrapper()));
	}
}
