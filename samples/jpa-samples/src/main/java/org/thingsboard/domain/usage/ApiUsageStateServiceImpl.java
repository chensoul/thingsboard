package org.thingsboard.domain.usage;

import java.io.Serializable;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.common.model.HasId;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
public class ApiUsageStateServiceImpl implements ApiUsageStateService {
	@Override
	public ApiUsageState createDefaultApiUsageState(String id, Serializable entityId) {
		return null;
	}

	@Override
	public ApiUsageState update(ApiUsageState apiUsageState) {
		return null;
	}

	@Override
	public ApiUsageState findTenantApiUsageState(String tenantId) {
		return null;
	}

	@Override
	public ApiUsageState findApiUsageStateByEntityId(Serializable entityId) {
		return null;
	}

	@Override
	public void deleteApiUsageStateByTenantId(String tenantId) {

	}

	@Override
	public void deleteApiUsageStateByEntityId(Serializable entityId) {

	}

	@Override
	public ApiUsageState findApiUsageStateById(String tenantId, Long id) {
		return null;
	}

	@Override
	public Optional<HasId<Long>> findEntity(Long id) {
		return Optional.empty();
	}

	@Override
	public EntityType getEntityType() {
		return null;
	}
}
