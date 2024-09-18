package org.thingsboard.server.ws.handler;

import org.thingsboard.server.ws.WebSocketSessionRef;
import org.thingsboard.server.ws.cmd.WsCmd;
import org.thingsboard.server.ws.cmd.WsCmdType;

public interface WsCmdHandler<C extends WsCmd> {
	WsCmdType getType();

	void handle(WebSocketSessionRef sessionRef, WsCmd cmd);
}
