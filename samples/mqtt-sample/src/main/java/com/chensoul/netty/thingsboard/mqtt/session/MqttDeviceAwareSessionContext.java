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
package com.chensoul.netty.thingsboard.mqtt.session;

import com.chensoul.netty.thingsboard.mqtt.auth.DeviceAuthService;
import com.chensoul.netty.thingsboard.mqtt.topic.MqttTopicMatcher;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Created by ashvayka on 30.08.18.
 */

public abstract class MqttDeviceAwareSessionContext extends DeviceAwareSessionContext {

	private final ConcurrentMap<MqttTopicMatcher, Integer> mqttQoSMap;

	public MqttDeviceAwareSessionContext(UUID sessionId, ConcurrentMap<MqttTopicMatcher, Integer> mqttQoSMap, DeviceAuthService authService) {
		super(sessionId, authService);
		this.mqttQoSMap = mqttQoSMap;
	}

	public ConcurrentMap<MqttTopicMatcher, Integer> getMqttQoSMap() {
		return mqttQoSMap;
	}

	public MqttQoS getQoSForTopic(String topic) {
		List<Integer> qosList = mqttQoSMap.entrySet()
			.stream()
			.filter(entry -> entry.getKey().matches(topic))
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());
		if (!qosList.isEmpty()) {
			return MqttQoS.valueOf(qosList.get(0));
		} else {
			return MqttQoS.AT_LEAST_ONCE;
		}
	}
}
