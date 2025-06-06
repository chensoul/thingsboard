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
package org.thingsboard.client.ws;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLParameters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.thingsboard.common.util.JacksonUtil;

@Slf4j
public class WsClient extends WebSocketClient implements AutoCloseable {
	public volatile JsonNode lastMsg;
	private CountDownLatch reply;
	private CountDownLatch update;

	private final Lock updateLock = new ReentrantLock();

	private long requestTimeoutMs;

	public WsClient(URI serverUri, long requestTimeoutMs) {
		super(serverUri);
		this.requestTimeoutMs = requestTimeoutMs;
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {

	}

	@Override
	public void onMessage(String s) {
		if (s == null) {
			return;
		}
		updateLock.lock();
		try {
			lastMsg = JacksonUtil.readTree(s);
			log.trace("Received new msg: {}", lastMsg.toPrettyString());
			if (update != null) {
				update.countDown();
			}
			if (reply != null) {
				reply.countDown();
			}
		} finally {
			updateLock.unlock();
		}
	}

	@Override
	public void onClose(int i, String s, boolean b) {
		log.debug("WebSocket client is closed");
	}

	@Override
	public void onError(Exception e) {
		log.error("WebSocket client error:", e);
	}

	public void registerWaitForUpdate() {
		updateLock.lock();
		try {
			lastMsg = null;
			update = new CountDownLatch(1);
		} finally {
			updateLock.unlock();
		}
		log.trace("Registered wait for update");
	}

	@Override
	public void send(String text) throws NotYetConnectedException {
		updateLock.lock();
		try {
			reply = new CountDownLatch(1);
		} finally {
			updateLock.unlock();
		}
		super.send(text);
	}

	public JsonNode waitForUpdate(long ms) {
		log.trace("update latch count: {}", update.getCount());
		try {
			if (update.await(ms, TimeUnit.MILLISECONDS)) {
				log.trace("Waited for update");
				return getLastMsg();
			}
		} catch (InterruptedException e) {
			log.debug("Failed to await reply", e);
		}
		log.trace("No update arrived within {} ms", ms);
		return null;
	}

	public JsonNode waitForReply() {
		try {
			if (reply.await(requestTimeoutMs, TimeUnit.MILLISECONDS)) {
				log.trace("Waited for reply");
				return getLastMsg();
			}
		} catch (InterruptedException e) {
			log.debug("Failed to await reply", e);
		}
		log.trace("No reply arrived within {} ms", requestTimeoutMs);
		throw new IllegalStateException("No WS reply arrived within " + requestTimeoutMs + " ms");
	}

	private JsonNode getLastMsg() {
		if (lastMsg != null) {
			JsonNode errorMsg = lastMsg.get("errorMsg");
			if (errorMsg != null && !errorMsg.isNull() && StringUtils.isNotEmpty(errorMsg.asText())) {
				throw new RuntimeException("WS error from server: " + errorMsg.asText());
			} else {
				return lastMsg;
			}
		} else {
			return null;
		}
	}

	@Override
	protected void onSetSSLParameters(SSLParameters sslParameters) {
		sslParameters.setEndpointIdentificationAlgorithm(null);
	}

}
