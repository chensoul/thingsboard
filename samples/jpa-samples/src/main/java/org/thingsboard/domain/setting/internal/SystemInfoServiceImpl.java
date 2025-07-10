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

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static org.thingsboard.common.util.SystemUtil.getCpuCount;
import static org.thingsboard.common.util.SystemUtil.getCpuUsage;
import static org.thingsboard.common.util.SystemUtil.getDiscSpaceUsage;
import static org.thingsboard.common.util.SystemUtil.getMemoryUsage;
import static org.thingsboard.common.util.SystemUtil.getTotalDiscSpace;
import static org.thingsboard.common.util.SystemUtil.getTotalMemory;
import org.thingsboard.domain.notification.internal.channel.mail.MailService;
import org.thingsboard.domain.notification.internal.channel.sms.SmsService;
import org.thingsboard.domain.oauth2.OAuth2Service;
import org.thingsboard.domain.setting.ServiceFeature;
import org.thingsboard.domain.setting.ServiceInfoProvider;
import org.thingsboard.domain.setting.ServiceInfo;
import org.thingsboard.domain.setting.ServiceInfoService;
import org.thingsboard.domain.setting.SystemSetting;
import org.thingsboard.domain.setting.SystemSettingService;
import org.thingsboard.domain.setting.SystemSettingType;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
public class SystemInfoServiceImpl implements ServiceInfoService {
	@Autowired
	private OAuth2Service oAuth2Service;

	@Autowired
	private MailService mailService;

	@Autowired
	private SmsService smsService;

	@Autowired
	private SystemSettingService systemSettingService;

	@Autowired
	private ServiceInfoProvider serviceInfoProvider;

	@Override
	public ServiceInfo getServiceInfo() {
		ServiceInfo serviceInfo = new ServiceInfo();

		ServiceInfo.SystemInfoData systemInfoData = new ServiceInfo.SystemInfoData();
		systemInfoData.setServiceId(serviceInfoProvider.getServiceId());
		systemInfoData.setServiceName(serviceInfoProvider.getServiceName());
//		systemInfoData.setServiceType(serviceInfoProvider.getServiceType());
		getCpuUsage().ifPresent(t -> systemInfoData.setCpuUsage(t));
		getMemoryUsage().ifPresent(t -> systemInfoData.setMemoryUsage(t));
		getDiscSpaceUsage().ifPresent(t -> systemInfoData.setDiscUsage(t));
		getCpuCount().ifPresent(t -> systemInfoData.setCpuCount(t));
		getTotalMemory().ifPresent(t -> systemInfoData.setTotalMemory(t));
		getTotalDiscSpace().ifPresent(t -> systemInfoData.setTotalDiscSpace(t));

		serviceInfo.setServiceInfos(Arrays.asList(systemInfoData));
		return serviceInfo;
	}

	@Override
	public ServiceFeature getFeatureInfo() {
		ServiceFeature featuresInfo = new ServiceFeature();
		featuresInfo.setEmailEnabled(isEmailEnabled());
		featuresInfo.setSmsEnabled(smsService.isConfigured(SYS_TENANT_ID));
		featuresInfo.setOauthEnabled(oAuth2Service.findOAuth2Info().isEnabled());
		featuresInfo.setTwoFaEnabled(isTwoFaEnabled());
//		featuresInfo.setNotificationEnabled(isSlackEnabled());
		return featuresInfo;
	}

	private boolean isEmailEnabled() {
		try {
			mailService.testConnection(SYS_TENANT_ID);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isTwoFaEnabled() {
		SystemSetting twoFaSettings = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.MFA);
		if (twoFaSettings != null) {
			var providers = twoFaSettings.getExtra().get("providers");
			return providers != null && providers.size() > 0;
		}
		return false;
	}
}
