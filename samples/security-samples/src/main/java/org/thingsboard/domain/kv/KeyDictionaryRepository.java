package org.thingsboard.domain.kv;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Repository
public interface KeyDictionaryRepository extends JpaRepository<KeyDictionaryEntity, Long> {
}
