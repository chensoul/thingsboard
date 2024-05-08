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

import java.io.IOException;
import org.springframework.web.socket.CloseStatus;

/**
 * Created by ashvayka on 27.03.18.
 */
public interface WebSocketMsgEndpoint {
	boolean contains(WebSocketSessionRef sessionRef);

	void send(WebSocketSessionRef sessionRef, int cmdId, String msg);

	void sendPing();

	void close(WebSocketSessionRef sessionRef, CloseStatus withReason) throws IOException;
}
