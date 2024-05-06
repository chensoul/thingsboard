package org.thingsboard.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.thingsboard.common.exception.ErrorResponse;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.concurrent.ThingsBoardExecutors;
import org.thingsboard.domain.usage.limit.LimitedApi;
import org.thingsboard.domain.usage.limit.RateLimitService;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.jwt.JwtAuthenticationProvider;
import org.thingsboard.server.ws.cmd.WsCmd;
import org.thingsboard.server.ws.cmd.WsCmdType;
import org.thingsboard.server.ws.cmd.WsCmdWrapper;
import org.thingsboard.server.ws.handler.WsCmdHandler;
import org.thingsboard.server.ws.handler.WsCmdService;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultWebSocketService implements WebSocketService {
	public static final int NUMBER_OF_PING_ATTEMPTS = 3;
	private final ConcurrentMap<String, WebSocketHandler.SessionMetaData> internalSessionMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> externalSessionMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, WebSocketSessionRef> blacklistedSessions = new ConcurrentHashMap<>();

	private final JwtAuthenticationProvider authenticationProvider;
	private final WebSocketApiLimitService webSocketApiLimitService;
	private final RateLimitService rateLimitService;

	@Value("${server.ws.send_timeout:5000}")
	private long sendTimeout;
	@Value("${server.ws.ping_timeout:30000}")
	private long pingTimeout;
	@Value("${server.ws.auth_timeout_ms:10000}")
	private int authTimeoutMs;

	private ExecutorService executor;
	private ScheduledExecutorService pingExecutor;
	private Map<WsCmdType, WsCmdHandler<? extends WsCmd>> cmdHandlers;
	private Cache<String, WebSocketHandler.SessionMetaData> pendingSessions;

	@PostConstruct
	public void init() {
		executor = ThingsBoardExecutors.newWorkStealingPool(50, getClass());
		pingExecutor = Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder().namingPattern("websocket-ping-%d").build());
		pingExecutor.scheduleWithFixedDelay(this::sendPing, pingTimeout / NUMBER_OF_PING_ATTEMPTS, pingTimeout / NUMBER_OF_PING_ATTEMPTS, TimeUnit.MILLISECONDS);

		cmdHandlers = new EnumMap<>(WsCmdType.class);
		cmdHandlers.put(WsCmdType.ATTRIBUTE, WsCmdService.newCmdHandler(WsCmdService::handleWsAttributesSubscriptionCmd));

		pendingSessions = Caffeine.newBuilder()
			.expireAfterWrite(authTimeoutMs, TimeUnit.MILLISECONDS)
			.<String, WebSocketHandler.SessionMetaData>removalListener((sessionId, sessionMd, removalCause) -> {
				if (removalCause == RemovalCause.EXPIRED && sessionMd != null) {
					try {
						closeSession(sessionMd.getSessionRef(), CloseStatus.POLICY_VIOLATION);
					} catch (IOException e) {
						log.warn("IO error", e);
					}
				}
			})
			.build();
	}


	@PreDestroy
	public void shutdownExecutor() {
		if (pingExecutor != null) {
			pingExecutor.shutdownNow();
		}

		if (executor != null) {
			executor.shutdownNow();
		}
	}

	private void sendPing() {
		long currentTime = System.currentTimeMillis();
		executor.submit(() -> {
			internalSessionMap.values().forEach(sessionMd -> {
				sessionMd.sendPing(currentTime);
			});
		});
	}

	@Override
	public void addSession(WebSocketSession session) throws IOException {
		if (session instanceof NativeWebSocketSession) {
			Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
			if (nativeSession != null) {
				nativeSession.getAsyncRemote().setSendTimeout(sendTimeout);
			}
		}

		WebSocketSessionRef sessionRef = toRef(session);
		log.info("[{}][{}] Session opened from address: {}", sessionRef.getSessionId(), session.getId(), session.getRemoteAddress());

		establishSession(session, sessionRef, null);
	}

	private void establishSession(WebSocketSession session, WebSocketSessionRef sessionRef, WebSocketHandler.SessionMetaData sessionMd) throws IOException {
		if (sessionRef.getSecurityCtx() != null) {
			if (webSocketApiLimitService.checkLimited(session, sessionRef)) {
				return;
			}
			if (sessionMd == null) {
				sessionMd = new WebSocketHandler.SessionMetaData(this, session, sessionRef, pingTimeout);
			}
			sessionMd.setMaxMsgQueueSize(webSocketApiLimitService.getMaxMsgQueueSize(sessionRef));

			internalSessionMap.put(session.getId(), sessionMd);
			externalSessionMap.put(sessionRef.getSessionId(), session.getId());
			processInWebSocketService(session, sessionRef, SessionEvent.onEstablished());
			log.info("[{}][{}][{}][{}] Session established from address: {}", sessionRef.getSecurityCtx().getTenantId(),
				sessionRef.getSecurityCtx().getId(), sessionRef.getSessionId(), session.getId(), session.getRemoteAddress());
		} else {
			sessionMd = new WebSocketHandler.SessionMetaData(this, session, sessionRef, pingTimeout);
			pendingSessions.put(session.getId(), sessionMd);
			externalSessionMap.put(sessionRef.getSessionId(), session.getId());
		}
	}

	@Override
	public void closeSession(WebSocketSessionRef sessionRef, CloseStatus reason) throws IOException {
		String externalId = sessionRef.getSessionId();
		log.debug("{} Processing close request", sessionRef);
		String internalId = externalSessionMap.get(externalId);
		if (internalId != null) {
			WebSocketHandler.SessionMetaData sessionMd = internalSessionMap.get(internalId);
			if (sessionMd != null) {
				sessionMd.getSession().close(reason);
			} else {
				log.warn("[{}][{}] Failed to find session by internal id", externalId, internalId);
			}
		} else {
			log.warn("[{}] Failed to find session by external id", externalId);
		}
	}

	@Override
	public void removeSession(WebSocketSession session, CloseStatus status) throws IOException {
		WebSocketHandler.SessionMetaData sessionMd = internalSessionMap.remove(session.getId());
		if (sessionMd == null) {
			sessionMd = pendingSessions.asMap().remove(session.getId());
		}

		if (sessionMd != null) {
			externalSessionMap.remove(sessionMd.getSessionRef().getSessionId());
			if (sessionMd.getSessionRef().getSecurityCtx() != null) {
				webSocketApiLimitService.cleanupLimits(session, sessionMd.getSessionRef());
				blacklistedSessions.remove(sessionMd.getSessionRef().getSessionId());
				handleSessionEvent(session, SessionEvent.onClosed());
			}
			log.warn("{} Session is closed", sessionMd.getSessionRef());
		} else {
			log.warn("[{}] Session is closed", session.getId());
		}
	}

	@Override
	public void handleSessionEvent(WebSocketSession session, SessionEvent event) {
		WebSocketHandler.SessionMetaData sessionMd = internalSessionMap.get(session.getId());
		if (sessionMd == null) {
			return;
		}

		log.warn("[{}] Session transport error", sessionMd.getSession().getId(), event);

		String sessionId = sessionMd.getSessionRef().getSessionId();
		switch (event.getEventType()) {
			case ESTABLISHED:
				break;
			case ERROR:
				log.debug("[{}] Unknown websocket session error: {}. ", sessionId, event.getError().orElse(null));
				break;
			case CLOSED:
				webSocketApiLimitService.processSessionClose(sessionMd.getSessionRef());
				break;
		}
	}

	@Override
	public void handMessage(WebSocketSession session, String message) {
		try {
			WebSocketHandler.SessionMetaData sessionMd = getSessionMd(session.getId());
			if (sessionMd == null) {
				log.warn("[{}] Failed to find session", session.getId());
				session.close(CloseStatus.SERVER_ERROR.withReason("Session not found!"));
				return;
			}
			sessionMd.onMsg(message);
		} catch (IOException e) {
			log.warn("IO error", e);
		}
	}

	public void processMsg(WebSocketHandler.SessionMetaData sessionMd, String msg) throws IOException {
		WebSocketSessionRef sessionRef = sessionMd.getSessionRef();
		log.trace("{} Processing {}", sessionRef, msg);

		WsCmdWrapper cmdsWrapper;
		try {
			cmdsWrapper = JacksonUtil.fromString(msg, WsCmdWrapper.class);
		} catch (Exception e) {
			log.debug("{} Failed to decode message: {}", sessionRef, e.getMessage(), e);
			sendError(sessionRef, 1, "Failed to decode message");
			return;
		}
		if (sessionRef.getSecurityCtx() != null) {
			log.trace("{} Processing {}", sessionRef, msg);
			handleCommands(sessionRef, cmdsWrapper);
		} else {
			String token = cmdsWrapper.getToken();
			if (token == null) {
				closeSession(sessionMd.getSessionRef(), CloseStatus.POLICY_VIOLATION.withReason("Auth Token is missing"));
				return;
			}
			log.trace("{} Authenticating session", sessionRef);
			SecurityUser securityCtx;
			try {
				securityCtx = authenticationProvider.authenticate(token);
			} catch (Exception e) {
				closeSession(sessionMd.getSessionRef(), CloseStatus.BAD_DATA.withReason(e.getMessage()));
				return;
			}
			sessionRef.setSecurityCtx(securityCtx);
			pendingSessions.invalidate(sessionMd.getSession().getId());
			establishSession(sessionMd.getSession(), sessionRef, sessionMd);

			handleCommands(sessionRef, cmdsWrapper);
		}
	}

	@Override
	public void handPong(WebSocketSession session, PongMessage message) {
		try {
			WebSocketHandler.SessionMetaData sessionMd = getSessionMd(session.getId());
			if (sessionMd != null) {
				log.trace("{} Processing pong response {}", sessionMd.getSessionRef(), message.getPayload());
				sessionMd.setLastActivityTime(System.currentTimeMillis());
			} else {
				log.trace("[{}] Failed to find session", session.getId());
				session.close(CloseStatus.SERVER_ERROR.withReason("Session not found!"));
			}
		} catch (IOException e) {
			log.warn("IO error", e);
		}
	}

	@Override
	public void handleCommands(WebSocketSessionRef sessionRef, WsCmdWrapper commandsWrapper) {
		if (commandsWrapper == null || CollectionUtils.isEmpty(commandsWrapper.getCmds())) {
			return;
		}
		String sessionId = sessionRef.getSessionId();
		for (WsCmd cmd : commandsWrapper.getCmds()) {
			log.debug("[{}][{}][{}] Processing cmd: {}", sessionId, cmd.getType(), cmd.getCmdId(), cmd);
			try {
				Optional.ofNullable(cmdHandlers.get(cmd.getType()))
					.ifPresent(cmdHandler -> cmdHandler.handle(sessionRef, cmd));
			} catch (Exception e) {
				log.error("[sessionId: {}, tenantId: {}, userId: {}] Failed to handle WS cmd: {}", sessionId,
					sessionRef.getSecurityCtx().getTenantId(), sessionRef.getSecurityCtx().getId(), cmd, e);
			}
		}
	}

	@Override
	public void sendError(WebSocketSessionRef sessionRef, int errorCode, String errorMsg) {
		sendResponse(sessionRef, ErrorResponse.of(errorMsg, ThingsboardErrorCode.GENERAL, HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Override
	public void send(WebSocketSessionRef sessionRef, String msg) throws IOException {
		log.debug("{} Sending {}", sessionRef, msg);
		String externalId = sessionRef.getSessionId();
		String internalId = externalSessionMap.get(externalId);
		if (internalId != null) {
			WebSocketHandler.SessionMetaData sessionMd = internalSessionMap.get(internalId);
			if (sessionMd != null) {
				String tenantId = sessionRef.getSecurityCtx().getTenantId();
				if (rateLimitService.checkRateLimited(LimitedApi.WS_UPDATES_PER_SESSION, tenantId, (Object) sessionRef.getSessionId())) {
					if (blacklistedSessions.putIfAbsent(externalId, sessionRef) == null) {
						log.info("{} Failed to process session update. Max session updates limit reached", sessionRef);
						sessionMd.sendMsg(ErrorResponse.of("Too many updates!", ThingsboardErrorCode.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS).toString());
					}
					return;
				} else {
					log.debug("{} Session is no longer blacklisted.", sessionRef);
					blacklistedSessions.remove(externalId);
				}
				sessionMd.sendMsg(msg);
			} else {
				log.warn("[{}][{}] Failed to find session by internal id", externalId, internalId);
			}
		} else {
			log.warn("[{}] Failed to find session by external id", externalId);
		}
	}

	@Override
	public void send(String msg) throws IOException {
		internalSessionMap.values().forEach(sessionMd -> {
			sessionMd.sendMsg(msg);
		});
	}

	private void sendResponse(WebSocketSessionRef sessionRef, Object update) {
		try {
			String msg = JacksonUtil.OBJECT_MAPPER.writeValueAsString(update);
			executor.submit(() -> {
				try {
					send(sessionRef, msg);
				} catch (IOException e) {
					log.warn("IO error", e);
				}
			});
		} catch (JsonProcessingException e) {
			log.warn("[{}] Failed to encode reply: {}", sessionRef.getSessionId(), update, e);
		}
	}

	private WebSocketSessionRef toRef(WebSocketSession session) {
		String token = StringUtils.substringAfter(session.getUri().getQuery(), "token=");

		SecurityUser securityCtx = null;
		if (StringUtils.isNotEmpty(token)) {
			securityCtx = authenticationProvider.authenticate(token);
		}

		return WebSocketSessionRef.builder()
			.sessionId(UUID.randomUUID().toString())
			.securityCtx(securityCtx)
			.localAddress(session.getLocalAddress())
			.remoteAddress(session.getRemoteAddress())
			.build();
	}

	private void processInWebSocketService(WebSocketSession session, WebSocketSessionRef sessionRef, SessionEvent event) {
		if (sessionRef.getSecurityCtx() == null) {
			return;
		}
		try {
			handleSessionEvent(session, event);
		} catch (BeanCreationNotAllowedException e) {
			log.warn("{} Failed to close session due to possible shutdown state", sessionRef);
		}
	}

	private WebSocketHandler.SessionMetaData getSessionMd(String internalSessionId) {
		WebSocketHandler.SessionMetaData sessionMd = internalSessionMap.get(internalSessionId);
		if (sessionMd == null) {
			sessionMd = pendingSessions.getIfPresent(internalSessionId);
		}
		return sessionMd;
	}
}
