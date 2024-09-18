package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.common.model.ToData;
import org.thingsboard.domain.oauth2.OAuth2Mobile;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface OAuth2MobileRepository extends JpaRepository<OAuth2MobileEntity, Long> {
	Collection<? extends ToData<OAuth2Mobile>> findByOauth2ParamId(Long oauth2ParamId);
}
