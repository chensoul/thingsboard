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
package com.chensoul.system.domain.usage.limit;

import com.chensoul.exception.BusinessException;
import com.chensoul.system.domain.audit.domain.EntityType;
import static com.chensoul.system.domain.audit.domain.EntityType.MERCHANT;
import static com.chensoul.system.domain.audit.domain.EntityType.TENANT;
import com.chensoul.system.infrastructure.security.rest.ErrorExceptionHandler;
import com.chensoul.system.infrastructure.security.util.SecurityUser;
import static com.chensoul.system.infrastructure.security.util.SecurityUtils.getCurrentUser;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitProcessingFilter extends OncePerRequestFilter {
    private final ErrorExceptionHandler errorResponseHandler;
    private final RateLimitService rateLimitService;

    @SneakyThrows
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        SecurityUser user = null;
        try {
            user = getCurrentUser();
        } catch (BusinessException e) {
            //ignore
        }
        if (user == null || user.isSystemAdmin()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (rateLimitService.checkRateLimited(LimitedApi.REST_REQUESTS_PER_TENANT, user.getTenantId())) {
                rateLimitExceeded(TENANT, response);
                return;
            }
        } catch (Exception e) {
            log.debug("[{}] Failed to lookup tenant profile", user.getTenantId());
            errorResponseHandler.handle(e, response);
            return;
        }

        if (user.isMerchantUser()) {
            if (rateLimitService.checkRateLimited(LimitedApi.REST_REQUESTS_PER_CUSTOMER, user.getTenantId(), user.getMerchantId())) {
                rateLimitExceeded(MERCHANT, response);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    private void rateLimitExceeded(EntityType type, HttpServletResponse response) {
        errorResponseHandler.handle(new RateLimitedException(type), response);
    }
}
