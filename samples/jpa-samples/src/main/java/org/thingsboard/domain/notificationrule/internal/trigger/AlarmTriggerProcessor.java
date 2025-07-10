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
package org.thingsboard.domain.notificationrule.internal.trigger;

import static com.google.common.collect.Iterables.isEmpty;
import java.util.Collection;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import org.springframework.stereotype.Service;
import org.thingsboard.domain.iot.alarm.Alarm;
import org.thingsboard.domain.iot.alarm.AlarmApiCallResult;
import org.thingsboard.domain.iot.alarm.AlarmStatusFilter;
import org.thingsboard.domain.iot.alarm.AlarmInfo;
import org.thingsboard.domain.notification.internal.info.AlarmNotificationInfo;
import org.thingsboard.domain.notification.RuleOriginatedNotificationInfo;
import org.thingsboard.domain.notificationrule.NotificationRuleTriggerProcessor;
import org.thingsboard.domain.notificationrule.NotificationRuleTriggerType;
import org.thingsboard.domain.notificationrule.AlarmNotificationRuleTriggerConfig;

@Service
public class AlarmTriggerProcessor implements NotificationRuleTriggerProcessor<AlarmTrigger, AlarmNotificationRuleTriggerConfig> {

	@Override
	public boolean matchesFilter(AlarmTrigger trigger, AlarmNotificationRuleTriggerConfig triggerConfig) {
		AlarmApiCallResult alarmUpdate = trigger.getAlarmUpdate();
		Alarm alarm = alarmUpdate.getAlarm();
		if (!typeMatches(alarm, triggerConfig)) {
			return false;
		}

		if (alarmUpdate.isCreated()) {
			if (triggerConfig.getNotifyOn().contains(AlarmNotificationRuleTriggerConfig.AlarmAction.CREATED)) {
				return severityMatches(alarm, triggerConfig);
			}
		} else if (alarmUpdate.isSeverityChanged()) {
			if (triggerConfig.getNotifyOn().contains(AlarmNotificationRuleTriggerConfig.AlarmAction.SEVERITY_CHANGED)) {
				return severityMatches(alarmUpdate.getOld(), triggerConfig) || severityMatches(alarm, triggerConfig);
			} else {
				// if we haven't yet sent notification about the alarm
				return !severityMatches(alarmUpdate.getOld(), triggerConfig) && severityMatches(alarm, triggerConfig);
			}
		} else if (alarmUpdate.isAcknowledged()) {
			if (triggerConfig.getNotifyOn().contains(AlarmNotificationRuleTriggerConfig.AlarmAction.ACKNOWLEDGED)) {
				return severityMatches(alarm, triggerConfig);
			}
		} else if (alarmUpdate.isCleared()) {
			if (triggerConfig.getNotifyOn().contains(AlarmNotificationRuleTriggerConfig.AlarmAction.CLEARED)) {
				return severityMatches(alarm, triggerConfig);
			}
		}
		return false;
	}

	@Override
	public boolean matchesClearRule(AlarmTrigger trigger, AlarmNotificationRuleTriggerConfig triggerConfig) {
		AlarmApiCallResult alarmUpdate = trigger.getAlarmUpdate();
		Alarm alarm = alarmUpdate.getAlarm();
		if (!typeMatches(alarm, triggerConfig)) {
			return false;
		}
		if (alarmUpdate.isDeleted()) {
			return true;
		}
		AlarmNotificationRuleTriggerConfig.ClearRule clearRule = triggerConfig.getClearRule();
		if (clearRule != null) {
			if (isNotEmpty(clearRule.getAlarmStatuses())) {
				return AlarmStatusFilter.from(clearRule.getAlarmStatuses()).matches(alarm);
			}
		}
		return false;
	}

	private boolean severityMatches(Alarm alarm, AlarmNotificationRuleTriggerConfig triggerConfig) {
		return emptyOrContains(triggerConfig.getAlarmSeverities(), alarm.getSeverity());
	}

	private boolean typeMatches(Alarm alarm, AlarmNotificationRuleTriggerConfig triggerConfig) {
		return emptyOrContains(triggerConfig.getAlarmTypes(), alarm.getType());
	}

	@Override
	public RuleOriginatedNotificationInfo constructNotificationInfo(AlarmTrigger trigger) {
		AlarmApiCallResult alarmUpdate = trigger.getAlarmUpdate();
		AlarmInfo alarmInfo = alarmUpdate.getAlarm();
		return AlarmNotificationInfo.builder()
			.alarmId(alarmInfo.getId())
			.alarmType(alarmInfo.getType())
			.action(alarmUpdate.isCreated() ? "created" :
				alarmUpdate.isSeverityChanged() ? "severity changed" :
					alarmUpdate.isAcknowledged() ? "acknowledged" :
						alarmUpdate.isCleared() ? "cleared" :
							alarmUpdate.isDeleted() ? "deleted" : null)
			.alarmOriginator(alarmInfo.getOriginator())
			.alarmOriginatorName(alarmInfo.getOriginatorName())
			.alarmSeverity(alarmInfo.getSeverity())
			.alarmStatus(alarmInfo.getStatus())
			.acknowledged(alarmInfo.isAcknowledged())
			.cleared(alarmInfo.isCleared())
			.alarmCustomerId(alarmInfo.getMerchantId())
			.build();
	}

	@Override
	public NotificationRuleTriggerType getTriggerType() {
		return NotificationRuleTriggerType.ALARM;
	}

	public static <V> boolean emptyOrContains(Collection<V> collection, V element) {
		return isEmpty(collection) || collection.contains(element);
	}
}
