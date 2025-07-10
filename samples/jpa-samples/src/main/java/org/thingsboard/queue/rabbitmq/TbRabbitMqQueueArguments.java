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
package org.thingsboard.queue.rabbitmq;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${queue.type:null}'=='rabbitmq'")
public class TbRabbitMqQueueArguments {
	@Value("${queue.rabbitmq.queue-properties.core:}")
	private String coreProperties;

	@Getter
	private Map<String, Object> coreArgs;

	@PostConstruct
	private void init() {
		coreArgs = getArgs(coreProperties);
	}

	public static Map<String, Object> getArgs(String properties) {
		Map<String, Object> configs = new HashMap<>();
		if (StringUtils.isNotEmpty(properties)) {
			for (String property : properties.split(";")) {
				int delimiterPosition = property.indexOf(":");
				String key = property.substring(0, delimiterPosition);
				String strValue = property.substring(delimiterPosition + 1);
				configs.put(key, getObjectValue(strValue));
			}
		}
		return configs;
	}

	private static Object getObjectValue(String str) {
		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
			return Boolean.valueOf(str);
		} else if (isNumeric(str)) {
			return getNumericValue(str);
		}
		return str;
	}

	private static Object getNumericValue(String str) {
		if (str.contains(".")) {
			return Double.valueOf(str);
		} else {
			return Long.valueOf(str);
		}
	}

	private static final Pattern PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

	private static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		return PATTERN.matcher(strNum).matches();
	}
}
