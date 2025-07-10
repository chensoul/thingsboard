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

import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import org.thingsboard.domain.tenant.DefaultTenantProfileConfiguration;

public enum LimitedApi {

	ENTITY_EXPORT(DefaultTenantProfileConfiguration::getTenantEntityExportRateLimit, "entity version creation", true),
	ENTITY_IMPORT(DefaultTenantProfileConfiguration::getTenantEntityImportRateLimit, "entity version load", true),
	NOTIFICATION_REQUESTS(DefaultTenantProfileConfiguration::getTenantNotificationRequestsRateLimit, "notification requests", true),
	NOTIFICATION_REQUESTS_PER_RULE(DefaultTenantProfileConfiguration::getTenantNotificationRequestsPerRuleRateLimit, "notification requests per rule", false),
	REST_REQUESTS_PER_TENANT(DefaultTenantProfileConfiguration::getTenantServerRestLimit, "REST API requests", true),
	REST_REQUESTS_PER_CUSTOMER(DefaultTenantProfileConfiguration::getCustomerServerRestLimit, "REST API requests per customer", false),
	WS_UPDATES_PER_SESSION(DefaultTenantProfileConfiguration::getWsUpdatesPerSessionRateLimit, "WS updates per session", true),
	PASSWORD_RESET(false, true),
	TWO_FA_VERIFICATION_CODE_SEND(false, true),
	TWO_FA_VERIFICATION_CODE_CHECK(false, true);

	private Function<DefaultTenantProfileConfiguration, String> configExtractor;
	@Getter
	private final boolean perTenant;
	@Getter
	private boolean refillRateLimitIntervally;
	@Getter
	private String label;

	LimitedApi(Function<DefaultTenantProfileConfiguration, String> configExtractor, String label, boolean perTenant) {
		this.configExtractor = configExtractor;
		this.label = label;
		this.perTenant = perTenant;
	}

	LimitedApi(boolean perTenant, boolean refillRateLimitIntervally) {
		this.perTenant = perTenant;
		this.refillRateLimitIntervally = refillRateLimitIntervally;
	}

	LimitedApi(String label, boolean perTenant) {
		this.label = label;
		this.perTenant = perTenant;
	}

	public String getLimitConfig(DefaultTenantProfileConfiguration profileConfiguration) {
		return Optional.ofNullable(configExtractor)
			.map(extractor -> extractor.apply(profileConfiguration))
			.orElse(null);
	}

}
