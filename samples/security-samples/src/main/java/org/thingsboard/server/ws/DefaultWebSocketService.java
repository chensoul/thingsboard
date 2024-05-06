/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.ws.cmd.CmdUpdate;
import org.thingsboard.server.ws.cmd.WsCmd;
import org.thingsboard.server.ws.cmd.WsCmdType;
import org.thingsboard.server.ws.cmd.WsCommandWrapper;
import org.thingsboard.server.ws.handler.WsCmdHandler;
import org.thingsboard.server.ws.handler.WsCmdService;

/**
 * Created by ashvayka on 27.03.18.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultWebSocketService implements WebSocketService {
	public static final int UNKNOWN_SUBSCRIPTION_ID = 0;
	private static final String SESSION_META_DATA_NOT_FOUND = "Session meta-data not found!";

	private final WebSocketMsgEndpoint msgEndpoint;

	private Map<WsCmdType, WsCmdHandler<? extends WsCmd>> cmdsHandlers;

	@PostConstruct
	public void init() {
		cmdsHandlers = new EnumMap<>(WsCmdType.class);
		cmdsHandlers.put(WsCmdType.ATTRIBUTE, WsCmdService.newCmdHandler(WsCmdService::handleWsAttributesSubscriptionCmd));
	}

	@Override
	public void handleCommands(WebSocketSessionRef sessionRef, WsCommandWrapper commandsWrapper) {
		if (commandsWrapper == null || CollectionUtils.isEmpty(commandsWrapper.getCmds())) {
			return;
		}
		String sessionId = sessionRef.getSessionId();
		if (!msgEndpoint.validate(sessionId)) {
			log.warn("[{}] Session meta data not found. ", sessionId);
			sendError(sessionRef, UNKNOWN_SUBSCRIPTION_ID, SESSION_META_DATA_NOT_FOUND);
			return;
		}

		for (WsCmd cmd : commandsWrapper.getCmds()) {
			log.debug("[{}][{}][{}] Processing cmd: {}", sessionId, cmd.getType(), cmd.getCmdId(), cmd);
			try {
				Optional.ofNullable(cmdsHandlers.get(cmd.getType()))
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
	public void sendUpdate(WebSocketSessionRef sessionRef, CmdUpdate update) {
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
