package org.thingsboard.domain.oauth2.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface OAuth2ClientRegistrationTemplateRepository extends JpaRepository<OAuth2ClientRegistrationTemplateEntity, Long> {
	OAuth2ClientRegistrationTemplateEntity findByProviderId(String providerId);
}
