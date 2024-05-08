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
public interface OAuth2ParamRepository extends JpaRepository<OAuth2ParamEntity, Long> {
	List<OAuth2ParamEntity> findByTenantId(String tenantId);
}
