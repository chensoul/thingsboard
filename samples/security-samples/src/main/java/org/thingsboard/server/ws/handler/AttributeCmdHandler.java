package org.thingsboard.server.ws.handler;

import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;
import org.thingsboard.server.ws.WebSocketSessionRef;
import org.thingsboard.server.ws.cmd.AttributeCmd;
import org.thingsboard.server.ws.cmd.WsCmd;
import org.thingsboard.server.ws.cmd.WsCmdType;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@Component
public class AttributeCmdHandler implements WsCmdHandler<AttributeCmd> {
	@Override
	public WsCmdType getType() {
		return WsCmdType.ATTRIBUTE;
	}

	@Override
	public void handle(WebSocketSessionRef sessionRef, WsCmd cmd) {
		String sessionId = sessionRef.getSessionId();
		log.info("[{}] Processing attribute cmd: {}", sessionId, cmd);
	}
}
