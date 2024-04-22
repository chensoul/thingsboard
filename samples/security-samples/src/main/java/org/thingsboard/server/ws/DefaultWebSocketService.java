package org.thingsboard.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.thingsboard.common.exception.ErrorResponse;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.ThingsBoardExecutors;
import org.thingsboard.domain.usage.limit.LimitedApi;
import org.thingsboard.domain.usage.limit.RateLimitService;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.jwt.JwtAuthenticationProvider;
import org.thingsboard.server.ws.cmd.WsCmd;
import org.thingsboard.server.ws.cmd.WsCmdType;
import org.thingsboard.server.ws.cmd.WsCommandsWrapper;

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

	private ExecutorService executor;
	private ScheduledExecutorService pingExecutor;
	private Map<WsCmdType, DefaultWebSocketService.WsCmdHandler<? extends WsCmd>> cmdsHandlers;

	@PostConstruct
	public void init() {
		executor = ThingsBoardExecutors.newWorkStealingPool(50, getClass());
		pingExecutor = Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder().namingPattern("websocket-ping-%d").build());
		pingExecutor.scheduleWithFixedDelay(this::sendPing, pingTimeout / NUMBER_OF_PING_ATTEMPTS, pingTimeout / NUMBER_OF_PING_ATTEMPTS, TimeUnit.MILLISECONDS);

		cmdsHandlers = new EnumMap<>(WsCmdType.class);
		cmdsHandlers.put(WsCmdType.ATTRIBUTES, newCmdHandler(this::handleWsAttributesSubscriptionCmd));
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

		if (webSocketApiLimitService.checkLimited(session, sessionRef)) {
			return;
		}

		WebSocketHandler.SessionMetaData sessionMd = new WebSocketHandler.SessionMetaData(this, session, sessionRef, pingTimeout);
		sessionMd.setMaxMsgQueueSize(webSocketApiLimitService.getMaxMsgQueueSize(sessionRef));
		internalSessionMap.put(session.getId(), sessionMd);
		externalSessionMap.put(sessionRef.getSessionId(), session.getId());
		handleSessionEvent(session, SessionEvent.onEstablished());

		log.info("[{}][{}][{}][{}] Session established from address: {}", sessionRef.getSecurityCtx().getTenantId(),
			sessionRef.getSecurityCtx().getId(), sessionRef.getSessionId(), session.getId(), session.getRemoteAddress());
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
				handleSessionEvent(sessionMd.getSession(), SessionEvent.onClosed());
			} else {
				log.warn("[{}][{}] Failed to find session by internal id", externalId, internalId);
			}
		} else {
			log.warn("[{}] Failed to find session by external id", externalId);
		}
	}

	@Override
	public WebSocketHandler.SessionMetaData removeSession(WebSocketSession session, CloseStatus status) throws IOException {
		WebSocketHandler.SessionMetaData sessionMd = internalSessionMap.remove(session.getId());
		if (sessionMd != null) {
			externalSessionMap.remove(sessionMd.getSessionRef().getSessionId());
			if (sessionMd.getSessionRef().getSecurityCtx() != null) {
				webSocketApiLimitService.checkLimited(session, sessionMd.getSessionRef());
				handleSessionEvent(session, SessionEvent.onClosed());
			}
			log.warn("{} Session is closed", sessionMd.getSessionRef());
		} else {
			log.warn("[{}] Session is closed", session.getId());
		}
		return sessionMd;
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
			WebSocketHandler.SessionMetaData sessionMd = internalSessionMap.get(session.getId());
			if (sessionMd == null) {
				log.warn("[{}] Failed to find session", session.getId());
				session.close(CloseStatus.SERVER_ERROR.withReason("Session not found!"));
				return;
			}
			sessionMd.handMessage(message);
		} catch (IOException e) {
			log.warn("IO error", e);
		}
	}

	@Override
	public void handPong(WebSocketSession session) {
		WebSocketHandler.SessionMetaData sessionMd = internalSessionMap.get(session.getId());
		if (sessionMd != null) {
			log.trace("{} Pong received", sessionMd.getSessionRef());
			sessionMd.setLastActivityTime(System.currentTimeMillis());
		}
	}

	@Override
	public void handleCommands(WebSocketSessionRef sessionRef, WsCommandsWrapper commandsWrapper) {
		if (commandsWrapper == null || CollectionUtils.isEmpty(commandsWrapper.getCmds())) {
			return;
		}
		String sessionId = sessionRef.getSessionId();
		for (WsCmd cmd : commandsWrapper.getCmds()) {
			log.debug("[{}][{}][{}] Processing cmd: {}", sessionId, cmd.getType(), cmd.getCmdId(), cmd);
			try {
				Optional.ofNullable(cmdsHandlers.get(cmd.getType()))
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
		SecurityUser securityCtx = null;
		String token = StringUtils.substringAfter(session.getUri().getQuery(), "token=");
		if (StringUtils.isNotEmpty(token)) {
			securityCtx = authenticationProvider.authenticate(token);
		} else {
			throw new BadCredentialsException("Token is empty");
		}
		return WebSocketSessionRef.builder()
			.sessionId(UUID.randomUUID().toString())
			.securityCtx(securityCtx)
			.localAddress(session.getLocalAddress())
			.remoteAddress(session.getRemoteAddress())
			.build();
	}

	private void handleWsAttributesSubscriptionCmd(WebSocketSessionRef webSocketSessionRef, WsCmd wsCmd) {
		log.info("wsCmd: {}", wsCmd);
	}

	public static <C extends WsCmd> DefaultWebSocketService.WsCmdHandler<C> newCmdHandler(BiConsumer<WebSocketSessionRef, C> handler) {
		return new DefaultWebSocketService.WsCmdHandler<>(handler);
	}

	@RequiredArgsConstructor
	@Getter
	@SuppressWarnings("unchecked")
	public static class WsCmdHandler<C extends WsCmd> {
		protected final BiConsumer<WebSocketSessionRef, C> handler;

		public void handle(WebSocketSessionRef sessionRef, WsCmd cmd) {
			handler.accept(sessionRef, (C) cmd);
		}
	}
}
