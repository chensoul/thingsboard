package org.thingsboard.server.ws.handler;

import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.thingsboard.server.ws.WebSocketSessionRef;
import org.thingsboard.server.ws.cmd.WsCmd;

@RequiredArgsConstructor
@Getter
@SuppressWarnings("unchecked")
public class WsCmdHandler<C extends WsCmd> {
	protected final BiConsumer<WebSocketSessionRef, C> handler;

	public void handle(WebSocketSessionRef sessionRef, WsCmd cmd) {
		handler.accept(sessionRef, (C) cmd);
	}
}
