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

import java.net.URI;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WsClientFactory {
	@Value("${monitoring.ws.base_url}")
	private String baseUrl;
	@Value("${monitoring.ws.request_timeout_ms}")
	private int requestTimeoutMs;

	public WsClient createClient(String accessToken) throws Exception {
		URI uri = new URI(baseUrl + "/api/ws?token=" + accessToken);
		WsClient wsClient = new WsClient(uri, requestTimeoutMs);
		if (baseUrl.startsWith("wss")) {
			SSLContextBuilder builder = SSLContexts.custom();
			builder.loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true);
			wsClient.setSocketFactory(builder.build().getSocketFactory());
		}
		boolean connected = wsClient.connectBlocking(requestTimeoutMs, TimeUnit.MILLISECONDS);
		if (!connected) {
			throw new IllegalStateException("Failed to establish WS session");
		}
		return wsClient;
	}

}
