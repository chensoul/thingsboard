package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.List;
import org.thingsboard.data.dao.Dao;
import org.thingsboard.domain.oauth2.OAuth2Domain;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface OAuth2DomainDao extends Dao<OAuth2Domain, Long> {
	List<OAuth2Domain> findByOAuth2ParamId(Long oauth2ParamId);
}
