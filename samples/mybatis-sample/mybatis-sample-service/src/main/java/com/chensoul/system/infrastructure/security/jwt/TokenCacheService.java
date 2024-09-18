package com.chensoul.system.infrastructure.security.jwt;

public interface TokenCacheService {

    boolean isExpired(Long userId, String sessionId, long issueTime);

}
