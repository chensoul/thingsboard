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
package com.chensoul.system.domain.notification.domain.template;

public enum NotificationType {

    GENERAL,
    DEVICE_ACTIVITY,
    ENTITY_ACTION,
    ALARM_COMMENT,
    RULE_ENGINE_COMPONENT_LIFECYCLE_EVENT,
    ALARM_ASSIGNMENT,
    NEW_PLATFORM_VERSION,
    ENTITIES_LIMIT,
    API_USAGE_LIMIT,
    RATE_LIMITS,
    EDGE_CONNECTION,
    EDGE_COMMUNICATION_FAILURE

}
