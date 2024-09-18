package com.chensoul.system.infrastructure.security.jwt;

import com.chensoul.system.CacheConstants;
import com.chensoul.system.domain.user.service.event.UserAuthDataChangedEvent;
import java.util.Optional;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultTokenCacheService implements TokenCacheService {
    private final CacheManager cacheManager;

    @EventListener(classes = UserAuthDataChangedEvent.class)
    public void onUserAuthDataChanged(UserAuthDataChangedEvent event) {
        if (event.getId() != null) {
            log.info("User [{}] auth data has changed, set jwt token expired time to {}", event.getId(), event.getTs());

            cacheManager.getCache(CacheConstants.USERS_SESSION_INVALIDATION_CACHE).put(event.getId().toString(), event.getTs());
        }
    }

    @Override
    public boolean isExpired(Long userId, String sessionId, long issueTime) {
        if (isTokenExpired(userId.toString(), issueTime)) {
            return true;
        } else {
            return sessionId != null && isTokenExpired(sessionId, issueTime);
        }
    }

    private Boolean isTokenExpired(String sessionId, long issueTime) {
        return Optional.ofNullable(cacheManager.getCache(CacheConstants.USERS_SESSION_INVALIDATION_CACHE).get(sessionId))
            .map(op -> isTokenExpired(issueTime, (Long) op.get())).orElse(false);
    }

    private boolean isTokenExpired(long issueTime, Long expiredTime) {
        return MILLISECONDS.toSeconds(issueTime) < MILLISECONDS.toSeconds(expiredTime);
    }
}
