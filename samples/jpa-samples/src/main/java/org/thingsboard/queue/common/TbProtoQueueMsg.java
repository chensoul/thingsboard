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
package org.thingsboard.queue.common;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import org.thingsboard.queue.spi.TbQueueMsg;
import org.thingsboard.queue.spi.TbQueueMsgHeaders;

@Data
public class TbProtoQueueMsg<T extends Serializable> implements TbQueueMsg {

	private final UUID key;
	protected final T value;
	private final TbQueueMsgHeaders headers;

	public TbProtoQueueMsg(UUID key, T value) {
		this(key, value, new DefaultTbQueueMsgHeaders());
	}

	public TbProtoQueueMsg(UUID key, T value, TbQueueMsgHeaders headers) {
		this.key = key;
		this.value = value;
		this.headers = headers;
	}

	@Override
	public UUID getKey() {
		return key;
	}

	@Override
	public TbQueueMsgHeaders getHeaders() {
		return headers;
	}

	@Override
	public byte[] getData() {
		return value.toString().getBytes();
	}
}
