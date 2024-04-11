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
package org.thingsboard.domain.notification.persistence;


import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.notification.settings.NotificationSettings;
import org.thingsboard.domain.notification.settings.UserNotificationSettings;

public interface NotificationSettingsService {

	void saveNotificationSettings(String tenantId, NotificationSettings settings);

	NotificationSettings findNotificationSettings(String tenantId);

	void deleteNotificationSettings(String tenantId);

	UserNotificationSettings saveUserNotificationSettings(String tenantId, Long userId, UserNotificationSettings settings);

	UserNotificationSettings getUserNotificationSettings(String tenantId, Long userId, boolean format);

	void createDefaultNotificationConfigs(String tenantId) throws ThingsboardException;

	void updateDefaultNotificationConfigs(String tenantId) throws ThingsboardException;

}
