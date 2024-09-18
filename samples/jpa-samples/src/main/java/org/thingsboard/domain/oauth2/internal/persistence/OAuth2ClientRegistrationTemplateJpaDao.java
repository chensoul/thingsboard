package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.data.dao.DaoUtil;
import org.thingsboard.data.dao.jpa.JpaAbstractDao;
import org.thingsboard.data.dao.aspect.SqlDao;
import org.thingsboard.domain.oauth2.OAuth2ClientRegistrationTemplate;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@SqlDao
@RequiredArgsConstructor
@Component
public class OAuth2ClientRegistrationTemplateJpaDao extends JpaAbstractDao<OAuth2ClientRegistrationTemplateEntity, OAuth2ClientRegistrationTemplate, Long> implements OAuth2ClientRegistrationTemplateDao {
	private final OAuth2ClientRegistrationTemplateRepository repository;

	@Override
	protected Class<OAuth2ClientRegistrationTemplateEntity> getEntityClass() {
		return OAuth2ClientRegistrationTemplateEntity.class;
	}

	@Override
	protected JpaRepository<OAuth2ClientRegistrationTemplateEntity, Long> getRepository() {
		return repository;
	}

	@Override
	public OAuth2ClientRegistrationTemplate findByProviderId(String providerId) {
		return DaoUtil.getData(repository.findByProviderId(providerId));
	}

	@Override
	public List<OAuth2ClientRegistrationTemplate> findAll() {
		return DaoUtil.convertDataList(repository.findAll());
	}
}
