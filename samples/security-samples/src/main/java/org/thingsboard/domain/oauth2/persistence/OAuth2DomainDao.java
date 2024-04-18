package org.thingsboard.domain.oauth2.persistence;

import java.util.List;
import org.thingsboard.common.dao.Dao;
import org.thingsboard.domain.oauth2.model.OAuth2Domain;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface OAuth2DomainDao extends Dao<OAuth2Domain, Long> {
	List<OAuth2Domain> findByOAuth2ParamId(Long oauth2ParamId);
}
