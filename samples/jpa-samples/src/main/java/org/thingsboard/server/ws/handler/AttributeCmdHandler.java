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
