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
package org.thingsboard.domain.notification.targets;

import java.util.Arrays;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.thingsboard.domain.notification.template.NotificationDeliveryType;

@RequiredArgsConstructor
public enum NotificationTargetType {

	PLATFORM_USER(Set.of(NotificationDeliveryType.WEB, NotificationDeliveryType.EMAIL, NotificationDeliveryType.SMS, NotificationDeliveryType.MOBILE_APP)),
	SLACK(Set.of(NotificationDeliveryType.SLACK)),
	MICROSOFT_TEAM(Set.of(NotificationDeliveryType.MICROSOFT_TEAM));

	@Getter
	private final Set<NotificationDeliveryType> supportedDeliveryTypes;

	public static NotificationTargetType forDeliveryMethod(NotificationDeliveryType deliveryType) {
		return Arrays.stream(values())
			.filter(targetType -> targetType.getSupportedDeliveryTypes().contains(deliveryType))
			.findFirst().orElse(null);
	}

}
