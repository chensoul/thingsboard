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
package org.thingsboard.domain.notification.internal.targets;

import java.util.Arrays;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.thingsboard.domain.notification.internal.template.NotificationDeliveryMethod;

@RequiredArgsConstructor
public enum NotificationTargetType {

	PLATFORM_USER(Set.of(NotificationDeliveryMethod.WEB, NotificationDeliveryMethod.EMAIL, NotificationDeliveryMethod.SMS, NotificationDeliveryMethod.MOBILE_APP)),
	SLACK(Set.of(NotificationDeliveryMethod.SLACK)),
	MICROSOFT_TEAM(Set.of(NotificationDeliveryMethod.MICROSOFT_TEAM));

	@Getter
	private final Set<NotificationDeliveryMethod> supportedDeliveryTypes;

	public static NotificationTargetType forDeliveryMethod(NotificationDeliveryMethod deliveryType) {
		return Arrays.stream(values())
			.filter(targetType -> targetType.getSupportedDeliveryTypes().contains(deliveryType))
			.findFirst().orElse(null);
	}

}
