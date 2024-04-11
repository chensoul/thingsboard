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
package org.thingsboard.domain.setting.system.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.service.BaseController;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.setting.system.model.SystemParam;
import org.thingsboard.domain.tenant.model.DefaultTenantProfileConfiguration;
import org.thingsboard.domain.user.model.UserSetting;
import org.thingsboard.domain.user.model.UserSettingType;
import org.thingsboard.server.security.SecurityUser;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;

@Hidden
@RestController
@RequestMapping("/api")
@Slf4j
public class SystemInfoController extends BaseController {
	private JsonNode buildInfo;

	@Value("${security.user_token_access_enabled}")
	private boolean userTokenAccessEnabled;

	@Value("${state.persistToTelemetry:false}")
	private boolean persistToTelemetry;

	@Autowired(required = false)
	private BuildProperties buildProperties;

	@Autowired
	private ConfigurableApplicationContext context;

	@PostConstruct
	public void init() {
		buildInfo = buildInfoObject();
		log.info("System build info: {}", buildInfo);
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
	@RequestMapping(value = "/system/info", method = RequestMethod.GET)
	@ResponseBody
	public JsonNode getSystemVersionInfo() {
		return buildInfo;
	}

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
	@RequestMapping(value = "/system/param", method = RequestMethod.GET)
	@ResponseBody
	public SystemParam getSystemParam() throws ThingsboardException {
		SystemParam systemParam = new SystemParam();
		SecurityUser currentUser = getCurrentUser();
		String tenantId = currentUser.getTenantId();
		Long customerId = currentUser.getMerchantId();
		if (currentUser.isSystemAdmin() || currentUser.isTenantAdmin()) {
			systemParam.setUserTokenAccessEnabled(userTokenAccessEnabled);
		} else {
			systemParam.setUserTokenAccessEnabled(false);
		}
		if (currentUser.isTenantAdmin() || currentUser.isMerchantUser()) {
			systemParam.setPersistDeviceStateToTelemetry(persistToTelemetry);
		} else {
			systemParam.setPersistDeviceStateToTelemetry(false);
		}
		UserSetting userSettings = userSettingService.findUserSetting(currentUser.getId(), UserSettingType.GENERAL);
		ObjectNode userSettingsNode = userSettings == null ? JacksonUtil.newObjectNode() : (ObjectNode) userSettings.getExtra();
		if (!userSettingsNode.has("openedMenuSections")) {
			userSettingsNode.set("openedMenuSections", JacksonUtil.newArrayNode());
		}
		systemParam.setUserSetting(userSettingsNode);
		if (!currentUser.isSystemAdmin()) {
			DefaultTenantProfileConfiguration tenantProfileConfiguration = tenantProfileService.findDefaultTenantProfile(tenantId).getDefaultProfileConfiguration();
			systemParam.setMaxResourceSize(tenantProfileConfiguration.getMaxResourceSize());
		}
		return systemParam;
	}

	private JsonNode buildInfoObject() {
		ObjectNode buildInfo = JacksonUtil.newObjectNode();
		if (buildProperties != null) {
			buildInfo.put("version", buildProperties.getVersion());
			buildInfo.put("artifact", buildProperties.getArtifact());
			buildInfo.put("name", buildProperties.getName());
		} else {
			buildInfo.put("version", "unknown");
			buildInfo.put("name", context.getApplicationName());
		}
		return buildInfo;
	}
}
