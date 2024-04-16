package org.thingsboard.server.ws;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.thingsboard.domain.tenant.model.DefaultTenantProfileConfiguration;
import org.thingsboard.domain.tenant.model.TenantProfile;
import org.thingsboard.domain.tenant.service.TenantProfileService;
import org.thingsboard.server.security.UserPrincipal;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@Component
public class WebSocketApiLimitService {
	@Value("${server.ws.max_queue_messages_per_session:1000}")
	private int wsMaxQueueMessagesPerSession;

	@Autowired
	private TenantProfileService tenantProfileService;

	private final ConcurrentMap<String, Set<String>> tenantSessionsMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Long, Set<String>> customerSessionsMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Long, Set<String>> regularUserSessionsMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Long, Set<String>> publicUserSessionsMap = new ConcurrentHashMap<>();

	public DefaultTenantProfileConfiguration getTenantProfileConfiguration(WebSocketSessionRef sessionRef) {
		return Optional.ofNullable(tenantProfileService.findDefaultTenantProfile())
			.map(TenantProfile::getDefaultProfileConfiguration).orElse(null);
	}

	public int getMaxMsgQueueSize(WebSocketSessionRef sessionRef) {
		return Optional.ofNullable(getTenantProfileConfiguration(sessionRef))
			.map(DefaultTenantProfileConfiguration::getWsMsgQueueLimitPerSession)
			.filter(profileLimit -> profileLimit > 0 && profileLimit < wsMaxQueueMessagesPerSession)
			.orElse(wsMaxQueueMessagesPerSession);
	}

	public boolean checkLimited(WebSocketSession session, WebSocketSessionRef sessionRef) throws IOException {
		DefaultTenantProfileConfiguration tenantProfileConfiguration = getTenantProfileConfiguration(sessionRef);
		if (tenantProfileConfiguration == null) {
			return false;
		}
		boolean limited;
		String sessionId = session.getId();
		if (tenantProfileConfiguration.getMaxWsSessionsPerTenant() > 0) {
			Set<String> tenantSessions = tenantSessionsMap.computeIfAbsent(sessionRef.getSecurityCtx().getTenantId(), id -> ConcurrentHashMap.newKeySet());
			synchronized (tenantSessions) {
				limited = tenantSessions.size() > tenantProfileConfiguration.getMaxWsSessionsPerTenant();
				if (!limited) {
					tenantSessions.add(sessionId);
				}
			}
			if (limited) {
				log.info("{} Failed to start session. Max tenant sessions limit reached", sessionRef.toString());
				session.close(CloseStatus.POLICY_VIOLATION.withReason("Max tenant sessions limit reached!"));
				return true;
			}
		}

		if (sessionRef.getSecurityCtx().isMerchantUser()) {
			if (tenantProfileConfiguration.getMaxWsSessionsPerCustomer() > 0) {
				Set<String> customerSessions = customerSessionsMap.computeIfAbsent(sessionRef.getSecurityCtx().getMerchantId(), id -> ConcurrentHashMap.newKeySet());
				synchronized (customerSessions) {
					limited = customerSessions.size() > tenantProfileConfiguration.getMaxWsSessionsPerCustomer();
					if (!limited) {
						customerSessions.add(sessionId);
					}
				}
				if (limited) {
					log.info("{} Failed to start session. Max customer sessions limit reached", sessionRef.toString());
					session.close(CloseStatus.POLICY_VIOLATION.withReason("Max customer sessions limit reached"));
					return true;
				}
			}
			if (tenantProfileConfiguration.getMaxWsSessionsPerRegularUser() > 0
				&& UserPrincipal.Type.USER_NAME.equals(sessionRef.getSecurityCtx().getUserPrincipal().getType())) {
				Set<String> regularUserSessions = regularUserSessionsMap.computeIfAbsent(sessionRef.getSecurityCtx().getId(), id -> ConcurrentHashMap.newKeySet());
				synchronized (regularUserSessions) {
					limited = regularUserSessions.size() > tenantProfileConfiguration.getMaxWsSessionsPerRegularUser();
					if (!limited) {
						regularUserSessions.add(sessionId);
					}
				}
				if (limited) {
					log.info("{} Failed to start session. Max regular user sessions limit reached", sessionRef.toString());
					session.close(CloseStatus.POLICY_VIOLATION.withReason("Max regular user sessions limit reached"));
					return true;
				}
			}
			if (tenantProfileConfiguration.getMaxWsSessionsPerPublicUser() > 0
				&& UserPrincipal.Type.PUBLIC_ID.equals(sessionRef.getSecurityCtx().getUserPrincipal().getType())) {
				Set<String> publicUserSessions = publicUserSessionsMap.computeIfAbsent(sessionRef.getSecurityCtx().getId(), id -> ConcurrentHashMap.newKeySet());
				synchronized (publicUserSessions) {
					limited = publicUserSessions.size() > tenantProfileConfiguration.getMaxWsSessionsPerPublicUser();
					if (!limited) {
						publicUserSessions.add(sessionId);
					}
				}
				if (limited) {
					log.info("{} Failed to start session. Max public user sessions limit reached", sessionRef.toString());
					session.close(CloseStatus.POLICY_VIOLATION.withReason("Max public user sessions limit reached"));
					return true;
				}
			}
		}
		return false;
	}

	public void processSessionClose(WebSocketSessionRef sessionRef) {
		var tenantProfileConfiguration = getTenantProfileConfiguration(sessionRef);
		if (tenantProfileConfiguration != null) {
			String sessionId = "[" + sessionRef.getSessionId() + "]";

			if (tenantProfileConfiguration.getMaxWsSubscriptionsPerTenant() > 0) {
				Set<String> tenantSubscriptions = tenantSessionsMap.computeIfAbsent(sessionRef.getSecurityCtx().getTenantId(), id -> ConcurrentHashMap.newKeySet());
				synchronized (tenantSubscriptions) {
					tenantSubscriptions.removeIf(subId -> subId.startsWith(sessionId));
				}
			}
			if (sessionRef.getSecurityCtx().isMerchantUser()) {
				if (tenantProfileConfiguration.getMaxWsSubscriptionsPerCustomer() > 0) {
					Set<String> customerSessions = customerSessionsMap.computeIfAbsent(sessionRef.getSecurityCtx().getMerchantId(), id -> ConcurrentHashMap.newKeySet());
					synchronized (customerSessions) {
						customerSessions.removeIf(subId -> subId.startsWith(sessionId));
					}
				}
				if (tenantProfileConfiguration.getMaxWsSubscriptionsPerRegularUser() > 0 && UserPrincipal.Type.USER_NAME.equals(sessionRef.getSecurityCtx().getUserPrincipal().getType())) {
					Set<String> regularUserSessions = regularUserSessionsMap.computeIfAbsent(sessionRef.getSecurityCtx().getId(), id -> ConcurrentHashMap.newKeySet());
					synchronized (regularUserSessions) {
						regularUserSessions.removeIf(subId -> subId.startsWith(sessionId));
					}
				}
				if (tenantProfileConfiguration.getMaxWsSubscriptionsPerPublicUser() > 0 && UserPrincipal.Type.PUBLIC_ID.equals(sessionRef.getSecurityCtx().getUserPrincipal().getType())) {
					Set<String> publicUserSessions = publicUserSessionsMap.computeIfAbsent(sessionRef.getSecurityCtx().getId(), id -> ConcurrentHashMap.newKeySet());
					synchronized (publicUserSessions) {
						publicUserSessions.removeIf(subId -> subId.startsWith(sessionId));
					}
				}
			}
		}
	}
}
