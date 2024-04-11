package org.thingsboard.server.ws;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.SendHandler;
import jakarta.websocket.SendResult;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.thingsboard.common.util.JacksonUtil;
import static org.thingsboard.server.ws.DefaultWebSocketService.NUMBER_OF_PING_ATTEMPTS;
import org.thingsboard.server.ws.cmd.WsCommandsWrapper;
import org.thingsboard.server.ws.message.PingWebSocketMsg;
import org.thingsboard.server.ws.message.TextWebSocketMsg;
import org.thingsboard.server.ws.message.WebSocketMsg;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
	private final WebSocketService webSocketService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		try {
			webSocketService.addSession(session);
		} catch (Exception e) {
			log.warn("[{}] Failed to start session", session.getId(), e);
			session.close(CloseStatus.SERVER_ERROR.withReason(e.getMessage()));
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		webSocketService.handMessage(session, message.getPayload());
	}

	@Override
	protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
		webSocketService.handPong(session);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable tError) throws Exception {
		webSocketService.handleSessionEvent(session, SessionEvent.onError(tError));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		webSocketService.removeSession(session, status);
	}

	@Data
	public static class SessionMetaData implements SendHandler {
		private final WebSocketService webSocketService;
		private final RemoteEndpoint.Async asyncRemote;
		private final WebSocketSessionRef sessionRef;

		private final AtomicBoolean isSending = new AtomicBoolean(false);
		private final Queue<WebSocketMsg<?>> outboundMsgQueue = new ConcurrentLinkedQueue<>();
		private final AtomicInteger outboundMsgQueueSize = new AtomicInteger();
		private final Queue<String> inboundMsgQueue = new ConcurrentLinkedQueue<>();
		private final Lock inboundMsgQueueProcessorLock = new ReentrantLock();

		private volatile long lastActivityTime;
		private long pingTimeout;

		@Setter
		private int maxMsgQueueSize;
		@Getter
		private final WebSocketSession session;

		public SessionMetaData(WebSocketService webSocketService, WebSocketSession session, WebSocketSessionRef sessionRef, long pingTimeout) {
			this.webSocketService = webSocketService;
			this.session = session;
			Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
			this.asyncRemote = nativeSession.getAsyncRemote();
			this.sessionRef = sessionRef;
			this.pingTimeout = pingTimeout;
			this.lastActivityTime = System.currentTimeMillis();
		}

		public void handMessage(String msg) throws IOException {
			inboundMsgQueue.add(msg);

			while (!inboundMsgQueue.isEmpty() && inboundMsgQueueProcessorLock.tryLock()) {
				try {
					String todo;
					while ((todo = inboundMsgQueue.poll()) != null) {
						processMsg(this, todo);
					}
				} finally {
					inboundMsgQueueProcessorLock.unlock();
				}
			}
		}

		void processMsg(WebSocketHandler.SessionMetaData sessionMd, String msg) throws IOException {
			WebSocketSessionRef sessionRef = sessionMd.sessionRef;
			log.trace("{} Processing {}", sessionRef, msg);

			WsCommandsWrapper cmdsWrapper;
			try {
				cmdsWrapper = JacksonUtil.fromString(msg, WsCommandsWrapper.class);
			} catch (Exception e) {
				log.debug("{} Failed to decode message: {}", sessionRef, e.getMessage(), e);
				webSocketService.sendError(sessionRef, 1, "Failed to decode message");
				return;
			}
			webSocketService.handleCommands(sessionRef, cmdsWrapper);
		}

		public void sendPing(long currentTime) {
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

		public void sendMsg(String msg) {
			sendMsg(new TextWebSocketMsg(msg));
		}

		void sendMsg(WebSocketMsg<?> msg) {
			if (outboundMsgQueueSize.get() < maxMsgQueueSize) {
				outboundMsgQueue.add(msg);
				outboundMsgQueueSize.incrementAndGet();
				processNextMsg();
			} else {
				log.warn("{} Session closed due to updates queue size exceeded", sessionRef);
				closeSession(CloseStatus.POLICY_VIOLATION.withReason("Max pending updates limit reached!"));
			}
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

		private void sendMsgInternal(WebSocketMsg<?> msg) {
			try {
				if (msg instanceof TextWebSocketMsg) {
					TextWebSocketMsg textMsg = (TextWebSocketMsg) msg;
					this.asyncRemote.sendText(textMsg.getMsg(), this);
					// isSending status will be reset in the onResult method by call back
				} else if (msg instanceof PingWebSocketMsg) {
					PingWebSocketMsg pingMsg = (PingWebSocketMsg) msg;
					this.asyncRemote.sendPing(pingMsg.getMsg()); // blocking call
					log.trace("{} Sent ping msg", sessionRef, pingMsg.getMsg());
					isSending.set(false);
					processNextMsg();
				}
			} catch (Exception e) {
				log.trace("{} Failed to send msg", sessionRef, e);
				closeSession(CloseStatus.SESSION_NOT_RELIABLE);
			}
		}

		void closeSession(CloseStatus reason) {
			try {
				webSocketService.closeSession(this.sessionRef, reason);
			} catch (IOException ioe) {
				log.trace("{} Session transport error", sessionRef, ioe);
			} finally {
				outboundMsgQueue.clear();
			}
		}

		@Override
		public void onResult(SendResult result) {
			if (!result.isOK()) {
				log.warn("{} Failed to send msg", sessionRef, result.getException());
				closeSession(CloseStatus.SESSION_NOT_RELIABLE);
				return;
			}

			isSending.set(false);
			processNextMsg();
		}

	}
}
