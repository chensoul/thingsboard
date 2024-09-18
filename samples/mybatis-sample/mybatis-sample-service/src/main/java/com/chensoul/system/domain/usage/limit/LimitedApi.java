package com.chensoul.system.domain.usage.limit;

import com.chensoul.system.domain.tenant.domain.DefaultTenantProfileConfiguration;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;

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

    @Getter
    private final boolean perTenant;
    private Function<DefaultTenantProfileConfiguration, String> configExtractor;
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
