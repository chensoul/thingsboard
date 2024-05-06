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
package org.thingsboard.server.ws.message;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextWebSocketMsg implements WebSocketMsg<String> {

	private final String value;

	@Override
	public String getMsg() {
		return value;
	}

	public TbWebSocketMsgType getType() {
		return TbWebSocketMsgType.TEXT;
	}

}
