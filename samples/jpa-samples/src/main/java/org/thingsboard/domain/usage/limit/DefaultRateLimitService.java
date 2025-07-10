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
package org.thingsboard.domain.usage.limit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.common.model.EntityType;
import org.thingsboard.domain.tenant.TenantProfile;
import org.thingsboard.domain.tenant.TenantProfileService;
import org.thingsboard.server.security.SecurityUtils;

@Service
@Slf4j
public class DefaultRateLimitService implements RateLimitService {
	private TenantProfileService tenantProfileService;

	private Cache<RateLimitKey, RateLimits> rateLimits;

	public DefaultRateLimitService(TenantProfileService tenantProfileService,
								   @Value("${cache.rateLimits.timeToLiveInMinutes:120}") int rateLimitsTtl,
								   @Value("${cache.rateLimits.maxSize:200000}") int rateLimitsCacheMaxSize) {
		this.tenantProfileService = tenantProfileService;
		this.rateLimits = Caffeine.newBuilder()
			.expireAfterAccess(rateLimitsTtl, TimeUnit.MINUTES)
			.maximumSize(rateLimitsCacheMaxSize)
			.build();
	}

	@Override
	public boolean checkRateLimited(LimitedApi api, String tenantId) {
		return checkRateLimited(api, tenantId, EntityType.TENANT);
	}

	@Override
	public boolean checkRateLimited(LimitedApi api, String tenantId, Object level) {
		if (SecurityUtils.isSysTenantId(tenantId)) {
			return false;
		}
		TenantProfile tenantProfile = tenantProfileService.findTenantProfileByTenantId(tenantId);
		if (tenantProfile == null) {
			throw new DataValidationException("Tenant profile not found!");
		}

		String rateLimitConfig = tenantProfile.getProfileConfiguration().map(api::getLimitConfig).orElse(null);
		boolean limited = checkRateLimited(api, level, rateLimitConfig);
		if (limited) {
//			notificationRuleProcessor.process(RateLimitsTrigger.builder()
//				.tenantId(tenantId)
//				.api(api)
//				.limitLevel(level instanceof EntityId ? (EntityId) level : tenantId)
//				.limitLevelEntityName(null)
//				.build());
		}
		return limited;
	}

	@Override
	public boolean checkRateLimited(LimitedApi api, Object level, String rateLimitConfig) {
		RateLimitKey key = new RateLimitKey(api, level);
		if (StringUtils.isEmpty(rateLimitConfig)) {
			rateLimits.invalidate(key);
			return false;
		}
		log.trace("[{}] Checking rate limit for {} ({})", level, api, rateLimitConfig);

		RateLimits rateLimit = rateLimits.asMap().compute(key, (k, limit) -> {
			if (limit == null || !limit.getConfiguration().equals(rateLimitConfig)) {
				limit = new RateLimits(rateLimitConfig, api.isRefillRateLimitIntervally());
				log.trace("[{}] Created new rate limit bucket for {} ({})", level, api, rateLimitConfig);
			}
			return limit;
		});
		boolean limited = !rateLimit.tryConsume();
		if (limited) {
			log.debug("[{}] Rate limit exceeded for {} ({})", level, api, rateLimitConfig);
		}
		return limited;
	}

	@Override
	public void cleanUp(LimitedApi api, Object level) {
		RateLimitKey key = new RateLimitKey(api, level);
		rateLimits.invalidate(key);
	}

	@Data(staticConstructor = "of")
	private static class RateLimitKey {
		private final LimitedApi api;
		private final Object level;
	}

}
