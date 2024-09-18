package org.thingsboard.data.dao;

import java.io.Serializable;
import java.util.Optional;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasId;

public interface EntityDaoService<I> {

	Optional<HasId<I>> findEntity(I id);

	default long countByTenantId(String tenantId) {
		throw new IllegalArgumentException("Not implemented for " + getEntityType());
	}

	default void deleteEntity(Serializable id) {
		throw new IllegalArgumentException(getEntityType().name() + " deletion not supported");
	}

	EntityType getEntityType();

}
