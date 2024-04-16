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
package org.thingsboard.server.security.jwt;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.setting.jwt.JwtSettingService;
import org.thingsboard.domain.user.event.UserAuthDataChangedEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultTokenCacheService implements TokenCacheService {
	private final StringRedisTemplate redisTemplate;
	private final JwtSettingService jwtSettingService;

	@EventListener(classes = UserAuthDataChangedEvent.class)
	public void onUserAuthDataChanged(UserAuthDataChangedEvent event) {
		if (event.getId() != null) {
			log.info("User [{}] auth data changed ad {}, update token redis cache", event.getId(), event.getTs());

			//缓存用户的 token 过期时间为当前事件的时间，并设置该缓存的过期时间为 jwt 过期时间
			redisTemplate.opsForValue().set(event.getId().toString(), String.valueOf(event.getTs()),
				jwtSettingService.getJwtSetting().getTokenExpirationTime(), TimeUnit.SECONDS);
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
		return Optional.ofNullable(redisTemplate.opsForValue().get(sessionId)).map(expiredTime -> isTokenExpired(issueTime, Long.parseLong(expiredTime))).orElse(false);
	}

	private boolean isTokenExpired(long issueTime, Long expiredTime) {
		return MILLISECONDS.toSeconds(issueTime) < MILLISECONDS.toSeconds(expiredTime);
	}
}
