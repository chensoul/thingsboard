/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.ws;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.SendHandler;
import jakarta.websocket.SendResult;
import jakarta.websocket.Session;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.thingsboard.common.concurrent.Executors;
import org.thingsboard.common.concurrent.ThreadFactory;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.usage.limit.LimitedApi;
import org.thingsboard.domain.usage.limit.RateLimitService;
import org.thingsboard.server.security.SecurityUser;
import org.thingsboard.server.security.jwt.JwtAuthenticationProvider;
import org.thingsboard.server.security.jwt.JwtExpiredTokenException;
import static org.thingsboard.server.ws.DefaultWebSocketService.UNKNOWN_SUBSCRIPTION_ID;
import org.thingsboard.server.ws.cmd.WsCmdWrapper;
import org.thingsboard.server.ws.message.PingWebSocketMsg;
import org.thingsboard.server.ws.message.TextWebSocketMsg;
import org.thingsboard.server.ws.message.WebSocketMsg;
import org.thingsboard.server.ws.message.WebSocketMsgType;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler implements WebSocketMsgEndpoint {
	public static final int NUMBER_OF_PING_ATTEMPTS = 3;

	private final ConcurrentMap<String, CustomSendHandler> internalSessionMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, String> externalSessionMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, WebSocketSessionRef> blacklistedSessions = new ConcurrentHashMap<>();

	@Lazy
	private final WebSocketService webSocketService;
	private final JwtAuthenticationProvider authenticationProvider;
	private final RateLimitService rateLimitService;
	private final WebSocketSessionLimitService webSocketSessionLimitService;
	private final ApplicationEventPublisher eventPublisher;

	@Value("${server.ws.send_timeout:5000}")
	private long sendTimeout;
	@Value("${server.ws.ping_timeout:30000}")
	private long pingTimeout;
	@Value("${server.ws.max_queue_messages_per_session:1000}")
	private int wsMaxQueueMessagesPerSession;
	@Value("${server.ws.auth_timeout_ms:10000}")
	private int authTimeoutMs;

	private ExecutorService executor;
	private ScheduledExecutorService pingExecutor;
	private Cache<String, CustomSendHandler> pendingSessions;

	@PostConstruct
	private void init() {
		executor = Executors.newWorkStealingPool(50, getClass());
		pingExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(ThreadFactory.forName("ws-ping"));
		pingExecutor.scheduleWithFixedDelay(this::sendPing, pingTimeout / NUMBER_OF_PING_ATTEMPTS, pingTimeout / NUMBER_OF_PING_ATTEMPTS, TimeUnit.MILLISECONDS);

		pendingSessions = Caffeine.newBuilder()
			.expireAfterWrite(authTimeoutMs, TimeUnit.MILLISECONDS)
			.<String, CustomSendHandler>removalListener((sessionId, sendHandler, removalCause) -> {
				if (removalCause == RemovalCause.EXPIRED && sendHandler != null) {
					try {
						close(sendHandler.sessionRef, CloseStatus.POLICY_VIOLATION);
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

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			CustomSendHandler sendHandler = getSendHandler(session.getId());
			if (sendHandler == null) {
				session.close(CloseStatus.SERVER_ERROR.withReason("Session not found"));
				return;
			}
			sendHandler.onMsg(message.getPayload());
		} catch (IOException e) {
			log.warn("IO error", e);
		}
	}

	@Override
	public void handlePongMessage(WebSocketSession session, PongMessage message) {
		try {
			CustomSendHandler sendHandler = getSendHandler(session.getId());
			if (sendHandler == null) {
				session.close(CloseStatus.SERVER_ERROR.withReason("Session not found"));
				return;
			}
			log.debug("{} Processing pong response {}", sendHandler.sessionRef, message.getPayload());
			sendHandler.processPongMessage(System.currentTimeMillis());
		} catch (IOException e) {
			log.warn("IO error", e);
		}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
		try {
			if (session instanceof NativeWebSocketSession) {
				Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
				if (nativeSession != null) {
					nativeSession.getAsyncRemote().setSendTimeout(sendTimeout);
				}
			}
			WebSocketSessionRef sessionRef = toRef(session);
			log.info("[{}][{}] Session opened from address: {}", sessionRef.getSessionId(), session.getId(), session.getRemoteAddress());
			establishSession(session, sessionRef, null);
		} catch (InvalidParameterException e) {
			log.warn("[{}] Failed to start session", session.getId(), e);
			session.close(CloseStatus.BAD_DATA.withReason(e.getMessage()));
		} catch (JwtExpiredTokenException e) {
			log.trace("[{}] Failed to start session", session.getId(), e);
			session.close(CloseStatus.SERVER_ERROR.withReason(e.getMessage()));
		} catch (Exception e) {
			log.warn("[{}] Failed to start session", session.getId(), e);
			session.close(CloseStatus.SERVER_ERROR.withReason(e.getMessage()));
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		super.afterConnectionClosed(session, closeStatus);
		CustomSendHandler sendHandler = internalSessionMap.remove(session.getId());
		if (sendHandler == null) {
			sendHandler = pendingSessions.asMap().remove(session.getId());
		}
		if (sendHandler != null) {
			externalSessionMap.remove(sendHandler.sessionRef.getSessionId());
			if (sendHandler.sessionRef.getSecurityCtx() != null) {
				webSocketSessionLimitService.cleanupLimits(session, sendHandler.sessionRef);
				processInWebSocketService(sendHandler.sessionRef, SessionEvent.onClosed());
			}
			log.info("{} Session is closed", sendHandler.sessionRef);
		} else {
			log.info("[{}] Session is closed", session.getId());
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable tError) throws Exception {
		super.handleTransportError(session, tError);
		CustomSendHandler sendHandler = getSendHandler(session.getId());
		if (sendHandler != null) {
			log.debug("[{}] Session transport error", session.getId(), tError);
			processInWebSocketService(sendHandler.sessionRef, SessionEvent.onError(tError));
		}
	}

	@Override
	public boolean contains(WebSocketSessionRef sessionRef) {
		return getSendHandlerByExternalId(sessionRef.getSessionId()) != null;
	}

	@Override
	public void send(WebSocketSessionRef sessionRef, int cmdId, String msg) {
		executor.submit(() -> {
			try {
				doSend(sessionRef, cmdId, msg);
			} catch (IOException e) {
				log.warn("[{}] Failed to send reply: {}", sessionRef.getSessionId(), msg, e);
			}
		});
	}

	@Override
	public void sendPing() {
		long currentTime = System.currentTimeMillis();
		internalSessionMap.values().forEach(sendHandler -> {
			executor.submit(() -> sendHandler.sendPing(currentTime));
		});
	}

	@Override
	public void close(WebSocketSessionRef sessionRef, CloseStatus reason) throws IOException {
		String externalId = sessionRef.getSessionId();
		CustomSendHandler sendHandler = getSendHandlerByExternalId(externalId);
		if (sendHandler == null) {
			return;
		}
		sendHandler.session.close(reason);
	}

	private void establishSession(WebSocketSession session, WebSocketSessionRef sessionRef, CustomSendHandler sendHandler) throws IOException {
		if (sessionRef.getSecurityCtx() != null) {
			if (webSocketSessionLimitService.checkLimited(session, sessionRef)) {
				return;
			}
			if (sendHandler == null) {
				sendHandler = new CustomSendHandler(session, sessionRef);
			}
			sendHandler.setMaxMsgQueueSize(webSocketSessionLimitService.getMaxMsgQueueSize(sessionRef));

			internalSessionMap.put(session.getId(), sendHandler);
			externalSessionMap.put(sessionRef.getSessionId(), session.getId());
			processInWebSocketService(sessionRef, SessionEvent.onEstablished());
			log.info("[{}][{}] Session established from user: {}@{}",
				sessionRef.getSessionId(), session.getId(), sessionRef.getSecurityCtx().getName(), sessionRef.getSecurityCtx().getTenantId());
		} else {
			sendHandler = new CustomSendHandler(session, sessionRef);
			pendingSessions.put(session.getId(), sendHandler);
			externalSessionMap.put(sessionRef.getSessionId(), session.getId());
		}
	}

	void processMsg(CustomSendHandler sendHandler, String msg) throws IOException {
		WebSocketSessionRef sessionRef = sendHandler.sessionRef;
		WsCmdWrapper cmdWrapper;
		try {
			cmdWrapper = JacksonUtil.fromString(msg, WsCmdWrapper.class);
		} catch (Exception e) {
			log.warn("{} Failed to decode cmd: {}", sessionRef, e.getMessage(), e);
			if (sessionRef.getSecurityCtx() != null) {
				webSocketService.sendError(sessionRef, UNKNOWN_SUBSCRIPTION_ID, "Failed to parse the payload");
			} else {
				close(sessionRef, CloseStatus.BAD_DATA.withReason(e.getMessage()));
			}
			return;
		}

		if (sessionRef.getSecurityCtx() != null) {
			log.debug("{} Processing {}", sessionRef, msg);
			webSocketService.handleCommand(sessionRef, cmdWrapper);
		} else {
			String token = cmdWrapper.getToken();
			if (token == null) {
				close(sessionRef, CloseStatus.POLICY_VIOLATION.withReason("Auth Token is missing"));
				return;
			}
			log.debug("{} Authenticating session", sessionRef);
			SecurityUser securityCtx;
			try {
				securityCtx = authenticationProvider.authenticate(token);
			} catch (Exception e) {
				close(sessionRef, CloseStatus.BAD_DATA.withReason(e.getMessage()));
				return;
			}
			sessionRef.setSecurityCtx(securityCtx);
			pendingSessions.invalidate(sendHandler.session.getId());
			establishSession(sendHandler.session, sessionRef, sendHandler);

			webSocketService.handleCommand(sessionRef, cmdWrapper);
		}
	}

	private void processInWebSocketService(WebSocketSessionRef sessionRef, SessionEvent event) {
		if (sessionRef.getSecurityCtx() != null) {
			eventPublisher.publishEvent(new WebSocketSessionEvent(new WebSocketSessionEvent.WebSocketSessionEventSource(sessionRef, event)));
		}
	}

	private WebSocketSessionRef toRef(WebSocketSession session) {
		SecurityUser securityCtx = null;
		String token = StringUtils.substringAfter(session.getUri().getQuery(), "token=");
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

	private void doSend(WebSocketSessionRef sessionRef, int cmdId, String msg) throws IOException {
		log.debug("{} Sending {}", sessionRef, msg);
		String externalId = sessionRef.getSessionId();
		CustomSendHandler sendHandler = getSendHandlerByExternalId(externalId);
		if (sendHandler == null) {
			return;
		}

		String tenantId = sessionRef.getSecurityCtx().getTenantId();
		if (rateLimitService.checkRateLimited(LimitedApi.WS_UPDATES_PER_SESSION, tenantId, (Object) sessionRef.getSessionId())) {
			if (blacklistedSessions.putIfAbsent(externalId, sessionRef) == null) {
				log.info("{} Failed to process session update. Max session updates limit reached", sessionRef);
				sendHandler.sendMsg("{\"cmdId\":" + cmdId + ", \"errorCode\":" + ThingsboardErrorCode.TOO_MANY_REQUESTS.getErrorCode() + ", \"errorMsg\":\"Too many updates\"}");
			}
			return;
		}
		sendHandler.sendMsg(msg);

		blacklistedSessions.remove(externalId);
		log.debug("{} Session is no longer blacklisted.", sessionRef);
	}

	private CustomSendHandler getSendHandler(String internalSessionId) {
		CustomSendHandler sendHandler = internalSessionMap.get(internalSessionId);
		if (sendHandler == null) {
			sendHandler = pendingSessions.getIfPresent(internalSessionId);
		}
		if (sendHandler == null) {
			log.warn("[{}] Failed to find session by internal id", internalSessionId);
		}
		return sendHandler;
	}

	private CustomSendHandler getSendHandlerByExternalId(String externalId) {
		String internalId = externalSessionMap.get(externalId);
		if (internalId == null) {
			log.warn("[{}] Failed to find session by external id", externalId);
			return null;
		}
		CustomSendHandler sendHandler = internalSessionMap.get(internalId);
		if (sendHandler == null) {
			log.warn("[{}][{}] Failed to find session by internal id", externalId, internalId);
		}
		return sendHandler;
	}

	class CustomSendHandler implements SendHandler {
		private final WebSocketSession session;
		private final RemoteEndpoint.Async asyncRemote;
		private final WebSocketSessionRef sessionRef;

		private final AtomicBoolean isSending = new AtomicBoolean(false);
		private final Queue<WebSocketMsg<?>> outboundMsgQueue = new ConcurrentLinkedQueue<>();
		private final AtomicInteger outboundMsgQueueSize = new AtomicInteger();
		private final Queue<String> inboundMsgQueue = new ConcurrentLinkedQueue<>();
		private final Lock inboundMsgQueueProcessorLock = new ReentrantLock();
		@Setter
		private int maxMsgQueueSize = wsMaxQueueMessagesPerSession;
		private volatile long lastActivityTime;

		CustomSendHandler(WebSocketSession session, WebSocketSessionRef sessionRef) {
			super();
			this.session = session;
			Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
			this.asyncRemote = nativeSession.getAsyncRemote();
			this.sessionRef = sessionRef;
			this.lastActivityTime = System.currentTimeMillis();
		}

		void sendPing(long currentTime) {
			try {
				long timeSinceLastActivity = currentTime - lastActivityTime;
				if (timeSinceLastActivity >= pingTimeout) {
					log.warn("{} Closing session due to ping timeout", sessionRef);
					closeSession(CloseStatus.SESSION_NOT_RELIABLE);
				} else if (timeSinceLastActivity >= pingTimeout / NUMBER_OF_PING_ATTEMPTS) {
					sendMsg(PingWebSocketMsg.INSTANCE);
				}
			} catch (Exception e) {
				log.trace("{} Failed to send ping msg", sessionRef, e);
				closeSession(CloseStatus.SESSION_NOT_RELIABLE);
			}
		}

		void closeSession(CloseStatus reason) {
			try {
				close(this.sessionRef, reason);
			} catch (IOException ioe) {
				log.trace("{} Session transport error", sessionRef, ioe);
			} finally {
				outboundMsgQueue.clear();
			}
		}

		void processPongMessage(long currentTime) {
			lastActivityTime = currentTime;
		}

		void sendMsg(String msg) {
			sendMsg(new TextWebSocketMsg(msg));
		}

		void sendMsg(WebSocketMsg<?> msg) {
			if (outboundMsgQueueSize.get() < maxMsgQueueSize) {
				outboundMsgQueue.add(msg);
				outboundMsgQueueSize.incrementAndGet();
				processNextMsg();
			} else {
				log.info("{} Session closed due to updates queue size exceeded", sessionRef);
				closeSession(CloseStatus.POLICY_VIOLATION.withReason("Max pending updates limit reached"));
			}
		}

		private void sendMsgInternal(WebSocketMsg<?> msg) {
			try {
				if (WebSocketMsgType.TEXT.equals(msg.getType())) {
					TextWebSocketMsg textMsg = (TextWebSocketMsg) msg;
					this.asyncRemote.sendText(textMsg.getMsg(), this);
					// isSending status will be reset in the onResult method by call back
				} else {
					PingWebSocketMsg pingMsg = (PingWebSocketMsg) msg;
					this.asyncRemote.sendPing(pingMsg.getMsg()); // blocking call
					isSending.set(false);
					processNextMsg();
				}
			} catch (Exception e) {
				log.trace("{} Failed to send msg", sessionRef, e);
				closeSession(CloseStatus.SESSION_NOT_RELIABLE);
			}
		}

		@Override
		public void onResult(SendResult result) {
			if (!result.isOK()) {
				log.trace("{} Failed to send msg", sessionRef, result.getException());
				closeSession(CloseStatus.SESSION_NOT_RELIABLE);
				return;
			}

			isSending.set(false);
			processNextMsg();
		}

		private void processNextMsg() {
			if (outboundMsgQueue.isEmpty() || !isSending.compareAndSet(false, true)) {
				return;
			}
			WebSocketMsg<?> msg = outboundMsgQueue.poll();
			if (msg != null) {
				outboundMsgQueueSize.decrementAndGet();
				sendMsgInternal(msg);
			} else {
				isSending.set(false);
			}
		}

		public void onMsg(String msg) throws IOException {
			inboundMsgQueue.add(msg);
			tryProcessInboundMsgs();
		}

		void tryProcessInboundMsgs() throws IOException {
			while (!inboundMsgQueue.isEmpty()) {
				if (!inboundMsgQueueProcessorLock.tryLock()) {
					return;
				}

				try {
					String msg;
					while ((msg = inboundMsgQueue.poll()) != null) {
						processMsg(this, msg);
					}
				} finally {
					inboundMsgQueueProcessorLock.unlock();
				}
			}
		}
	}
}
