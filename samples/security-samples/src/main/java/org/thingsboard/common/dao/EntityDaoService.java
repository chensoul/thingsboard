package org.thingsboard.common.dao;

import java.io.Serializable;
import java.util.Optional;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasId;

public interface EntityDaoService {

	Optional<HasId<? extends Serializable>> findEntity(Serializable id);

	default long countByTenantId(String tenantId) {
		throw new IllegalArgumentException("Not implemented for " + getEntityType());
	}

	default void deleteEntity(Serializable id) {
		throw new IllegalArgumentException(getEntityType().name() + " deletion not supported");
	}

	EntityType getEntityType();

}
