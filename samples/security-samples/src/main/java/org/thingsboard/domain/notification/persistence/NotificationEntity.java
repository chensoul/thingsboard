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

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Formula;
import org.thingsboard.common.dao.jpa.JsonConverter;
import org.thingsboard.common.dao.jpa.LongBaseEntity;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.notification.Notification;
import org.thingsboard.domain.notification.NotificationStatus;
import org.thingsboard.domain.notification.info.NotificationInfo;
import org.thingsboard.domain.notification.template.NotificationDeliveryMethod;
import org.thingsboard.domain.notification.template.NotificationType;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notification")
public class NotificationEntity extends LongBaseEntity<Notification> {

	@Column(nullable = false)
	private Long requestId;

	@Column(nullable = false)
	private Long recipientId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationDeliveryMethod deliveryMethod;

	private String subject;

	@Column(nullable = false)
	private String text;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "jsonb")
	private JsonNode config;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "jsonb")
	@Formula("(SELECT r.info FROM notification_request r WHERE r.id = request_id)")
	private JsonNode info;

	@Enumerated(EnumType.STRING)
	private NotificationStatus status;

	@Override
	public Notification toData() {
		Notification notification = new Notification();
		notification.setId(id);
		notification.setCreatedTime(createdTime);
		notification.setRequestId(requestId);
		notification.setRecipientId(recipientId);
		notification.setType(type);
		notification.setDeliveryMethod(deliveryMethod);
		notification.setSubject(subject);
		notification.setText(text);
		notification.setConfig(config);
		notification.setInfo(JacksonUtil.convertValue(info, NotificationInfo.class));
		notification.setStatus(status);
		return notification;
	}

}
