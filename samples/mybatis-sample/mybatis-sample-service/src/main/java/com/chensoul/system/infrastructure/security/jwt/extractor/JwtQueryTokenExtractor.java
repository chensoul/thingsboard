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
package com.chensoul.system.infrastructure.security.jwt.extractor;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component(value = "jwtQueryTokenExtractor")
public class JwtQueryTokenExtractor implements TokenExtractor {

    @Override
    public String extract(HttpServletRequest request) {
        String token = null;
        if (request.getParameterMap() != null && !request.getParameterMap().isEmpty()) {
            String[] tokenParamValue = request.getParameterMap().get(JWT_TOKEN_QUERY_PARAM);
            if (tokenParamValue != null && tokenParamValue.length == 1) {
                token = tokenParamValue[0];
            }
        }

        if (StringUtils.isBlank(token)) {
            token = request.getHeader(JWT_TOKEN_QUERY_PARAM);
        }

        if (StringUtils.isBlank(token)) {
            throw new AuthenticationServiceException("Authorization query parameter cannot be blank!");
        }

        return token;
    }
}
