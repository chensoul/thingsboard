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
package org.thingsboard.domain.setting.internal;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.setting.ServiceInfoProvider;


@RequiredArgsConstructor
@Component
@Slf4j
public class DefaultServiceInfoProvider implements ServiceInfoProvider {
	@Nullable
	private final BuildProperties buildProperties;

	private final ConfigurableApplicationContext context;

	@Getter
	@Value("${service.id:#{null}}")
	private String serviceId;

	@PostConstruct
	public void init() {
		if (StringUtils.isEmpty(serviceId)) {
			try {
				serviceId = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				serviceId = RandomStringUtils.randomAlphabetic(10);
			}
		}
		log.info("Current Service ID: {}", serviceId);
	}


	@Override
	public String getServiceName() {
		if (buildProperties != null) {
			return buildProperties.getName();
		} else {
			return context.getApplicationName();
		}
	}

	@Override
	public String getServiceType() {
		return "";
	}
}
