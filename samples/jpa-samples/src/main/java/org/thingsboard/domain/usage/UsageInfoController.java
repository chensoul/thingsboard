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
package org.thingsboard.domain.usage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.exception.ThingsboardException;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;

@RestController
@RequestMapping("/api")
@Slf4j
public class UsageInfoController {
	@Autowired
	private UsageInfoService usageInfoService;

	@PreAuthorize("hasAuthority('TENANT_ADMIN')")
	@RequestMapping(value = "/usage", method = RequestMethod.GET)
	public UsageInfo getTenantUsageInfo() {
		return usageInfoService.getUsageInfo(getCurrentUser().getTenantId());
	}
}
