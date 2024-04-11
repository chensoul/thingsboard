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
package org.thingsboard.domain.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import org.thingsboard.domain.notification.template.NotificationDeliveryType;

@Data
public class NotificationRequestStats {

	private final Map<NotificationDeliveryType, AtomicInteger> sent;
	@JsonIgnore
	private final AtomicInteger totalSent;
	private final Map<NotificationDeliveryType, Map<String, String>> errors;
	@JsonIgnore
	private final AtomicInteger totalErrors;
	private String error;
	@JsonIgnore
	private final Map<NotificationDeliveryType, Set<Object>> processedRecipients;

	public NotificationRequestStats() {
		this.sent = new ConcurrentHashMap<>();
		this.totalSent = new AtomicInteger();
		this.errors = new ConcurrentHashMap<>();
		this.totalErrors = new AtomicInteger();
		this.processedRecipients = new ConcurrentHashMap<>();
	}

	@JsonCreator
	public NotificationRequestStats(@JsonProperty("sent") Map<NotificationDeliveryType, AtomicInteger> sent,
									@JsonProperty("errors") Map<NotificationDeliveryType, Map<String, String>> errors,
									@JsonProperty("error") String error) {
		this.sent = sent;
		this.totalSent = null;
		this.errors = errors;
		this.totalErrors = null;
		this.error = error;
		this.processedRecipients = Collections.emptyMap();
	}

	public void reportSent(NotificationDeliveryType deliveryMethod, NotificationRecipient recipient) {
		sent.computeIfAbsent(deliveryMethod, k -> new AtomicInteger()).incrementAndGet();
		totalSent.incrementAndGet();
	}

	public void reportError(NotificationDeliveryType deliveryMethod, Throwable error, NotificationRecipient recipient) {
		if (error instanceof AlreadySentException) {
			return;
		}
		String errorMessage = error.getMessage();
		if (errorMessage == null) {
			errorMessage = error.getClass().getSimpleName();
		}
		errors.computeIfAbsent(deliveryMethod, k -> new ConcurrentHashMap<>()).put(recipient.getName(), errorMessage);
		totalErrors.incrementAndGet();
	}

	public void reportProcessed(NotificationDeliveryType deliveryMethod, Object recipientId) {
		processedRecipients.computeIfAbsent(deliveryMethod, k -> ConcurrentHashMap.newKeySet()).add(recipientId);
	}

	public boolean contains(NotificationDeliveryType deliveryMethod, Object recipientId) {
		Set<Object> processedRecipients = this.processedRecipients.get(deliveryMethod);
		return processedRecipients != null && processedRecipients.contains(recipientId);
	}

}
