package org.thingsboard.server.ws.handler;

import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.thingsboard.server.ws.WebSocketService;
import org.thingsboard.server.ws.WebSocketSessionRef;
import org.thingsboard.server.ws.cmd.AttributeCmd;
import org.thingsboard.server.ws.cmd.CmdErrorCode;
import org.thingsboard.server.ws.cmd.DataUpdate;
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
@RequiredArgsConstructor
public class AttributeCmdHandler implements WsCmdHandler<AttributeCmd> {
	private final WebSocketService webSocketService;

	@Override
	public WsCmdType getType() {
		return WsCmdType.ATTRIBUTE;
	}

	@Override
	public void handle(WebSocketSessionRef sessionRef, WsCmd cmd) {
		String sessionId = sessionRef.getSessionId();
		log.info("[{}] Processing attribute cmd: {}", sessionId, cmd);

		webSocketService.sendUpdate(sessionRef, new DataUpdate(cmd.getCmdId(), CmdErrorCode.NO_ERROR.getCode(), null));
	}
}
