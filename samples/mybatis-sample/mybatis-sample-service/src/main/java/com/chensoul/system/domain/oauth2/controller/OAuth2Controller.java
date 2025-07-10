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
package com.chensoul.system.domain.oauth2.controller;

import com.chensoul.spring.util.ServletUtils;
import com.chensoul.system.domain.oauth2.domain.OAuth2ClientInfo;
import com.chensoul.system.domain.oauth2.domain.OAuth2Info;
import com.chensoul.system.domain.oauth2.domain.PlatformType;
import com.chensoul.system.domain.oauth2.service.OAuth2Service;
import com.chensoul.system.infrastructure.security.oauth2.OAuth2Configuration;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class OAuth2Controller {
    private final OAuth2Service oAuth2Service;
    private final OAuth2Configuration oAuth2Configuration;

    @GetMapping("/noauth/oauth2Clients")
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
        return oAuth2Service.getOAuth2Clients(ServletUtils.getScheme(request), ServletUtils.getDomainNameAndPort(request), pkgName, platformType);
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @GetMapping("/oauth2/config")
    public OAuth2Info getCurrentOAuth2Info() {
        return oAuth2Service.findOAuth2Info();
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @PostMapping("/oauth2/config")
    public OAuth2Info saveOAuth2Info(@RequestBody OAuth2Info oauth2Info) {
        oAuth2Service.saveOAuth2Info(oauth2Info);
        return oAuth2Service.findOAuth2Info();
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @GetMapping("/oauth2/loginProcessingUrl")
    public String getLoginProcessingUrl() {
        return oAuth2Configuration.getLoginProcessingUrl();
    }

}
