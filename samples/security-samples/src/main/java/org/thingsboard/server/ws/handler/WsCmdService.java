package org.thingsboard.server.ws.handler;

import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.ws.WebSocketSessionRef;
import org.thingsboard.server.ws.cmd.WsCmd;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
public class WsCmdService {
	public static <C extends WsCmd> WsCmdHandler<C> newCmdHandler(BiConsumer<WebSocketSessionRef, C> handler) {
		return new WsCmdHandler<>(handler);
	}

	/**
	 * TODO: 改为依赖注入的方式
	 */
	public static void handleWsAttributesSubscriptionCmd(WebSocketSessionRef sessionRef, WsCmd wsCmd) {
		String sessionId = sessionRef.getSessionId();
		log.info("[{}] Processing attribute subscription cmd: {}", sessionId, wsCmd);
	}
}
