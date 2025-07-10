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
package org.thingsboard.queue.sqs;

import com.amazonaws.services.sqs.model.QueueAttributeName;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${queue.type:null}'=='aws-sqs'")
public class TbAwsSqsQueueAttributes {
	@Value("${queue.aws-sqs.queue-properties.core:}")
	private String coreProperties;

	@Getter
	private Map<String, String> coreAttributes;

	private final Map<String, String> defaultAttributes = new HashMap<>();

	@PostConstruct
	private void init() {
		defaultAttributes.put(QueueAttributeName.FifoQueue.toString(), "true");

		coreAttributes = getConfigs(coreProperties);
	}

	private Map<String, String> getConfigs(String properties) {
		Map<String, String> configs = new HashMap<>(defaultAttributes);
		configs.putAll(toConfigs(properties));
		return configs;
	}

	public static Map<String, String> toConfigs(String properties) {
		Map<String, String> configs = new HashMap<>();
		if (StringUtils.isNotEmpty(properties)) {
			for (String property : properties.split(";")) {
				int delimiterPosition = property.indexOf(":");
				String key = property.substring(0, delimiterPosition);
				String value = property.substring(delimiterPosition + 1);
				validateAttributeName(key);
				configs.put(key, value);
			}
		}
		return configs;
	}

	private static void validateAttributeName(String key) {
		QueueAttributeName.fromValue(key);
	}
}
