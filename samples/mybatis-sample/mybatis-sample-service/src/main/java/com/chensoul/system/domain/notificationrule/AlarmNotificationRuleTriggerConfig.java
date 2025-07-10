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
package com.chensoul.system.domain.notificationrule;

import com.chensoul.system.domain.notificationrule.internal.trigger.AlarmSearchStatus;
import com.chensoul.system.domain.notificationrule.internal.trigger.AlarmSeverity;
import java.io.Serializable;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmNotificationRuleTriggerConfig implements NotificationRuleTriggerConfig {

    private Set<String> alarmTypes;
    private Set<AlarmSeverity> alarmSeverities;
    @NotEmpty
    private Set<AlarmAction> notifyOn;

    private ClearRule clearRule;

    @Override
    public NotificationRuleTriggerType getTriggerType() {
        return NotificationRuleTriggerType.ALARM;
    }

    public enum AlarmAction {
        CREATED, SEVERITY_CHANGED, ACKNOWLEDGED, CLEARED
    }

    @Data
    public static class ClearRule implements Serializable {
        private Set<AlarmSearchStatus> alarmStatuses;
    }

}
