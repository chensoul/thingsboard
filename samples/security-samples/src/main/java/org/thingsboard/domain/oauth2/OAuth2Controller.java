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
package org.thingsboard.domain.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.util.MiscUtils;
import org.thingsboard.server.security.oauth2.OAuth2Configuration;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class OAuth2Controller {
	private final OAuth2Service oAuth2Service;

	@Autowired
	private OAuth2Configuration oAuth2Configuration;

	@RequestMapping(value = "/noauth/oauth2Clients", method = RequestMethod.POST)
	public List<OAuth2ClientInfo> getOAuth2Clients(HttpServletRequest request,
												   @RequestParam(required = false) String pkgName,
												   @RequestParam(required = false) String platform) {
		if (log.isDebugEnabled()) {
			log.debug("Executing getOAuth2Clients: [{}][{}][{}]", request.getScheme(), request.getServerName(), request.getServerPort());
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String header = headerNames.nextElement();
				log.debug("Header: {} {}", header, request.getHeader(header));
			}
		}
		PlatformType platformType = null;
		if (StringUtils.isNotEmpty(platform)) {
			try {
				platformType = PlatformType.valueOf(platform);
			} catch (Exception e) {
			}
		}
		return oAuth2Service.getOAuth2Clients(MiscUtils.getScheme(request), MiscUtils.getDomainNameAndPort(request), pkgName, platformType);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/oauth2/config", method = RequestMethod.GET, produces = "application/json")
	public OAuth2Info getCurrentOAuth2Info() {
		return oAuth2Service.findOAuth2Info();
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/oauth2/config", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public OAuth2Info saveOAuth2Info(@RequestBody OAuth2Info oauth2Info) {
		oAuth2Service.saveOAuth2Info(oauth2Info);
		return oAuth2Service.findOAuth2Info();
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/oauth2/loginProcessingUrl", method = RequestMethod.GET)
	public String getLoginProcessingUrl() {
		return oAuth2Configuration.getLoginProcessingUrl();
	}

}
