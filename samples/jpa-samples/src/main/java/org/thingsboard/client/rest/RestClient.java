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
package org.thingsboard.client.rest;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.common.concurrent.Executors;

/**
 * @author Andrew Shvayka
 */
public class RestClient implements Closeable {
	private static final String JWT_TOKEN_HEADER_PARAM = "Authorization";
	private static final long AVG_REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
	@Getter
	protected final RestTemplate restTemplate;
	protected final RestTemplate loginRestTemplate;
	protected final String baseURL;
	private final ExecutorService service = Executors.newWorkStealingPool(10, getClass());
	private String username;
	private String password;

	@Getter
	private String token;
	@Getter
	private String refreshToken;
	private long mainTokenExpTs;
	private long refreshTokenExpTs;
	private long clientServerTimeDiff;

	public RestClient(String baseURL) {
		this(new RestTemplate(), baseURL);
	}

	public RestClient(RestTemplate restTemplate, String baseURL) {
		this.restTemplate = restTemplate;
		this.loginRestTemplate = new RestTemplate(restTemplate.getRequestFactory());
		this.baseURL = baseURL;
		this.restTemplate.getInterceptors().add((request, bytes, execution) -> {
			HttpRequest wrapper = new HttpRequestWrapper(request);
			long calculatedTs = System.currentTimeMillis() + clientServerTimeDiff + AVG_REQUEST_TIMEOUT;
			if (calculatedTs > mainTokenExpTs) {
				synchronized (RestClient.this) {
					if (calculatedTs > mainTokenExpTs) {
						if (calculatedTs < refreshTokenExpTs) {
							refreshToken();
						} else {
							doLogin();
						}
					}
				}
			}
			wrapper.getHeaders().set(JWT_TOKEN_HEADER_PARAM, "Bearer " + token);
			return execution.execute(wrapper, bytes);
		});
	}

	public void refreshToken() {
		Map<String, String> refreshTokenRequest = new HashMap<>();
		refreshTokenRequest.put("refreshToken", refreshToken);
		long ts = System.currentTimeMillis();
		ResponseEntity<JsonNode> tokenInfo = loginRestTemplate.postForEntity(baseURL + "/api/auth/token", refreshTokenRequest, JsonNode.class);
		setTokenInfo(ts, tokenInfo.getBody());
	}

	public void login(String username, String password) {
		this.username = username;
		this.password = password;
		doLogin();
	}

	private void doLogin() {
		long ts = System.currentTimeMillis();
		Map<String, String> loginRequest = new HashMap<>();
		loginRequest.put("username", username);
		loginRequest.put("password", password);
		ResponseEntity<JsonNode> tokenInfo = loginRestTemplate.postForEntity(baseURL + "/api/auth/login", loginRequest, JsonNode.class);
		setTokenInfo(ts, tokenInfo.getBody());
	}

	private synchronized void setTokenInfo(long ts, JsonNode tokenInfo) {
		this.token = tokenInfo.get("token").asText();
		this.refreshToken = tokenInfo.get("refreshToken").asText();
		this.mainTokenExpTs = JWT.decode(this.token).getExpiresAtAsInstant().toEpochMilli();
		this.refreshTokenExpTs = JWT.decode(refreshToken).getExpiresAtAsInstant().toEpochMilli();
		this.clientServerTimeDiff = JWT.decode(this.token).getIssuedAtAsInstant().toEpochMilli() - ts;
	}

	@Override
	public void close() {
		service.shutdown();
	}

}
