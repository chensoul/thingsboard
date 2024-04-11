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
package org.thingsboard.domain.limit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.thingsboard.common.exception.RateLimitedException;
import org.thingsboard.common.exception.ErrorResponseExceptionHandler;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.model.EntityType;
import static org.thingsboard.common.model.EntityType.MERCHANT;
import static org.thingsboard.common.model.EntityType.TENANT;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.server.security.SecurityUser;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitProcessingFilter extends OncePerRequestFilter {
	private final ErrorResponseExceptionHandler errorResponseHandler;
	private final RateLimitService rateLimitService;

	@SneakyThrows
	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		SecurityUser user = null;
		try {
			user = getCurrentUser();
		} catch (ThingsboardException e) {
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
