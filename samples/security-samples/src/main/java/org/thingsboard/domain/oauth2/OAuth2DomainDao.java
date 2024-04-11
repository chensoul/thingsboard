package org.thingsboard.domain.oauth2;

import java.util.List;
import org.thingsboard.common.dao.Dao;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface OAuth2DomainDao extends Dao<OAuth2Domain> {
	List<OAuth2Domain> findByOAuth2ParamId(Long oauth2ParamsId);
}
