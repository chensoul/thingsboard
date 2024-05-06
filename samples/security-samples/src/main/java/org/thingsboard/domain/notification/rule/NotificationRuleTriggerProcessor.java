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
package org.thingsboard.domain.notification.rule;

import org.thingsboard.domain.notification.info.RuleOriginatedNotificationInfo;
import org.thingsboard.domain.notification.rule.trigger.NotificationRuleTrigger;
import org.thingsboard.domain.notification.rule.trigger.config.NotificationRuleTriggerConfig;
import org.thingsboard.domain.notification.rule.trigger.config.NotificationRuleTriggerType;

public interface NotificationRuleTriggerProcessor<T extends NotificationRuleTrigger, C extends NotificationRuleTriggerConfig> {

	boolean matchesFilter(T trigger, C triggerConfig);

	default boolean matchesClearRule(T trigger, C triggerConfig) {
		return false;
	}

	RuleOriginatedNotificationInfo constructNotificationInfo(T trigger);

	NotificationRuleTriggerType getTriggerType();

}
