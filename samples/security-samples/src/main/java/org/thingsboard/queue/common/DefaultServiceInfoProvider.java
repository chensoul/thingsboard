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
package org.thingsboard.queue.common;

import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.queue.ServiceType;

@Component
@Slf4j
public class DefaultServiceInfoProvider implements ServiceInfoProvider {

	@Getter
	@Value("${service.id:#{null}}")
	private String serviceId;

	@Getter
	@Value("${service.type:monolith}")
	private String serviceType;

	@Getter
	private List<ServiceType> serviceTypes;

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
		if (serviceType.equalsIgnoreCase("monolith")) {
			serviceTypes = List.of(ServiceType.values());
		} else {
			serviceTypes = Collections.singletonList(ServiceType.of(serviceType));
		}
	}


}
