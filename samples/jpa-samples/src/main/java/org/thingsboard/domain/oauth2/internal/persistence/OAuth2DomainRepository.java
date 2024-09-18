package org.thingsboard.domain.oauth2.internal.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface OAuth2DomainRepository extends JpaRepository<OAuth2DomainEntity, Long> {
	List<OAuth2DomainEntity> findByOauth2ParamId(Long oauth2ParamId);
}
