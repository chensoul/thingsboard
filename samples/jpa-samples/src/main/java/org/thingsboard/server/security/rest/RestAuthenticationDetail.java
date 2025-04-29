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
package org.thingsboard.server.security.rest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import ua_parser.Client;
import ua_parser.Parser;

@Data
public class RestAuthenticationDetail implements Serializable {
	private static final List<String> CLIENT_IP_HEADER_NAMES = Arrays.asList("X-Forwarded-For",
		"X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR");
	private static final String LOCAL_IP4 = "127.0.0.1";
	private static final String LOCAL_IP6 = "0:0:0:0:0:0:0:1";

	private final String serverAddress;
	private final String clientAddress;
    private final Client userAgent;

    public RestAuthenticationDetail(HttpServletRequest request) {
        this.clientAddress = getClientIP(request);
		this.serverAddress = handleIpv6(request.getLocalAddr());
        this.userAgent = getUserAgent(request);
    }

	private String getClientIP(HttpServletRequest request) {
		String ip = null;
		for (String header : CLIENT_IP_HEADER_NAMES) {
			ip = request.getHeader(header);
			if (ip != null) {
				break;
			}
		}

		if (ip == null) {
			ip = request.getRemoteAddr();
		}

		ip = ip.split(",")[0];
		return handleIpv6(ip);
    }

	private String handleIpv6(String ip) {
		if (ip.equals(LOCAL_IP6)) {
			return LOCAL_IP4;
		}
		return ip;
	}

	private Client getUserAgent(HttpServletRequest request) {
        Parser uaParser = new Parser();
        return uaParser.parse(request.getHeader("User-Agent"));
    }
}
