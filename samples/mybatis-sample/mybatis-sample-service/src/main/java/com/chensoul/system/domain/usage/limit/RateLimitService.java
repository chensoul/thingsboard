package com.chensoul.system.domain.usage.limit;


public interface RateLimitService {

    boolean checkRateLimited(LimitedApi api, String tenantId);

    boolean checkRateLimited(LimitedApi api, String tenantId, Object level);

    boolean checkRateLimited(LimitedApi api, Object level, String rateLimitConfig);

    void cleanUp(LimitedApi api, Object level);

}
