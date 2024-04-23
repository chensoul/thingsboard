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
package org.thingsboard.queue.common;

import java.util.concurrent.atomic.AtomicInteger;
import org.thingsboard.queue.msg.TbMsgCallback;
import org.thingsboard.queue.spi.TbQueueCallback;
import org.thingsboard.queue.spi.TbQueueMsgMetadata;

public class MultipleTbQueueTbMsgCallbackWrapper implements TbQueueCallback {

	private final AtomicInteger tbQueueCallbackCount;
	private final TbMsgCallback tbMsgCallback;

	public MultipleTbQueueTbMsgCallbackWrapper(int tbQueueCallbackCount, TbMsgCallback tbMsgCallback) {
		this.tbQueueCallbackCount = new AtomicInteger(tbQueueCallbackCount);
		this.tbMsgCallback = tbMsgCallback;
	}

	@Override
	public void onSuccess(TbQueueMsgMetadata metadata) {
		if (tbQueueCallbackCount.decrementAndGet() <= 0) {
			tbMsgCallback.onSuccess();
		}
	}

	@Override
	public void onFailure(Throwable t) {
		tbMsgCallback.onFailure(new Exception(t.getMessage(), t));
	}
}
