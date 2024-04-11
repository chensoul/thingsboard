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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thingsboard.domain.user.event.UserAuthDataChangedEvent;

@RequiredArgsConstructor
@Component
public class DefaultTokenOutdatingService implements TokenOutdatingService {
	//TODO
	private final Map<String, Long> cache = new HashMap<>();

	@EventListener(classes = UserAuthDataChangedEvent.class)
	public void onUserAuthDataChanged(UserAuthDataChangedEvent event) {
		if (StringUtils.hasText(event.getId())) {
			cache.put(event.getId(), event.getTs());
		}
	}

	@Override
	public boolean isOutdated(Long userId, String sessionId, long issueTime) {
		if (isTokenOutdated(userId.toString(), issueTime)) {
			return true;
		} else {
			return sessionId != null && isTokenOutdated(sessionId, issueTime);
		}
	}

	private Boolean isTokenOutdated(String sessionId, long issueTime) {
		return Optional.ofNullable(cache.get(sessionId)).map(outdatageTime -> isTokenOutdated(issueTime, outdatageTime)).orElse(false);
	}

	private boolean isTokenOutdated(long issueTime, Long outdatageTime) {
		return MILLISECONDS.toSeconds(issueTime) < MILLISECONDS.toSeconds(outdatageTime);
	}
}
