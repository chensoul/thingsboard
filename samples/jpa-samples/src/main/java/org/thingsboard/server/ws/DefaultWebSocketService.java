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

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.ws.cmd.DataUpdate;
import org.thingsboard.server.ws.cmd.WsCmd;
import org.thingsboard.server.ws.cmd.WsCmdType;
import org.thingsboard.server.ws.cmd.WsCmdWrapper;
import org.thingsboard.server.ws.handler.WsCmdHandler;

/**
 * Created by ashvayka on 27.03.18.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultWebSocketService implements WebSocketService {
	public static final int UNKNOWN_SUBSCRIPTION_ID = 0;

	@Lazy
	private final List<WsCmdHandler<? extends WsCmd>> cmdHandlers;
	private final WebSocketMsgEndpoint msgEndpoint;

	private Map<WsCmdType, WsCmdHandler<? extends WsCmd>> cmdHandlerMap = new HashMap<>();

	@PostConstruct
	public void init() {
		cmdHandlers.forEach(handler -> cmdHandlerMap.put(handler.getType(), handler));
	}

	@Override
	public void handleCommand(WebSocketSessionRef sessionRef, WsCmdWrapper commandsWrapper) {
		if (commandsWrapper == null || CollectionUtils.isEmpty(commandsWrapper.getCmds())) {
			return;
		}
		String sessionId = sessionRef.getSessionId();
		if (!msgEndpoint.contains(sessionRef)) {
			log.warn("[{}] Session not found. ", sessionId);
			sendError(sessionRef, UNKNOWN_SUBSCRIPTION_ID, "Session not found");
			return;
		}

		for (WsCmd cmd : commandsWrapper.getCmds()) {
			log.debug("[{}][{}][{}] Processing cmd: {}", sessionId, cmd.getType(), cmd.getCmdId(), cmd);
			try {
				Optional.ofNullable(cmdHandlerMap.get(cmd.getType()))
					.ifPresent(cmdHandler -> cmdHandler.handle(sessionRef, cmd));
			} catch (Exception e) {
				log.error("[sessionId: {}, tenantId: {}, userId: {}] Failed to handle WS cmd: {}", sessionId,
					sessionRef.getSecurityCtx().getTenantId(), sessionRef.getSecurityCtx().getId(), cmd, e);
			}
		}
	}

	@Override
	public void sendError(WebSocketSessionRef sessionRef, int cmdId, String errorMsg) {
		doSendUpdate(sessionRef, cmdId, errorMsg);
	}

	@Override
	public void sendUpdate(WebSocketSessionRef sessionRef, DataUpdate update) {
		doSendUpdate(sessionRef, update.getCmdId(), update);
	}

	private void doSendUpdate(WebSocketSessionRef sessionRef, int cmdId, Object update) {
		try {
			String msg = JacksonUtil.OBJECT_MAPPER.writeValueAsString(update);
			msgEndpoint.send(sessionRef, cmdId, msg);
		} catch (JsonProcessingException e) {
			log.warn("[{}] Failed to encode reply: {}", sessionRef.getSessionId(), update, e);
		}
	}
}
