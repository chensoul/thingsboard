package org.thingsboard.transport;

import java.net.InetSocketAddress;
import org.springframework.stereotype.Service;
import org.thingsboard.common.model.EntityType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
public class DefaultTransportRateLimitService implements TransportRateLimitService {
	@Override
	public EntityType checkLimits(String tenantId, String deviceId, int dataPoints) {
		return null;
	}

	@Override
	public void update(TenantProfileUpdateResult update) {

	}

	@Override
	public void update(String tenantId) {

	}

	@Override
	public void removeByTenantId(String tenantId) {

	}

	@Override
	public void remove(String deviceId) {

	}

	@Override
	public void update(String tenantId, boolean transportEnabled) {

	}

	@Override
	public boolean checkAddress(InetSocketAddress address) {
		return true;
	}

	@Override
	public void onAuthSuccess(InetSocketAddress address) {

	}

	@Override
	public void onAuthFailure(InetSocketAddress address) {

	}

	@Override
	public void invalidateRateLimitsIpTable(long sessionInactivityTimeout) {

	}
}
