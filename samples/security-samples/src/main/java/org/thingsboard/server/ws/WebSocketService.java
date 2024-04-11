package org.thingsboard.server.ws;

import java.io.IOException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.thingsboard.server.ws.cmd.WsCommandsWrapper;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface WebSocketService {
	void addSession(WebSocketSession session) throws IOException;

	void closeSession(WebSocketSessionRef sessionRef, CloseStatus reason) throws IOException;

	WebSocketHandler.SessionMetaData removeSession(WebSocketSession session, CloseStatus status) throws IOException;

	void handleSessionEvent(WebSocketSession session, SessionEvent sessionEvent);

	void handleCommands(WebSocketSessionRef sessionRef, WsCommandsWrapper commandsWrapper);

	void handMessage(WebSocketSession session, String message);

	void handPong(WebSocketSession session);

	void send(WebSocketSessionRef sessionRef, String msg) throws IOException;

	void send(String msg) throws IOException;

	void sendError(WebSocketSessionRef sessionRef, int errorCode, String errorMsg);
}
