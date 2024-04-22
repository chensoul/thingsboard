package org.thingsboard.domain.setting.system;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static org.thingsboard.common.util.SystemUtil.getCpuCount;
import static org.thingsboard.common.util.SystemUtil.getCpuUsage;
import static org.thingsboard.common.util.SystemUtil.getDiscSpaceUsage;
import static org.thingsboard.common.util.SystemUtil.getMemoryUsage;
import static org.thingsboard.common.util.SystemUtil.getTotalDiscSpace;
import static org.thingsboard.common.util.SystemUtil.getTotalMemory;
import org.thingsboard.domain.notification.channel.mail.MailService;
import org.thingsboard.domain.notification.channel.sms.SmsService;
import org.thingsboard.domain.oauth2.service.OAuth2Service;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
public class SystemInfoServiceImpl implements SystemInfoService {
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
	@Autowired
	private SystemSettingServiceImpl systemSettingServiceImpl;

	@Override
	public SystemInfo getSystemInfo() {
		SystemInfo systemInfo = new SystemInfo();


		SystemInfoData systemInfoData = new SystemInfoData();
		systemInfoData.setServiceId(serviceInfoProvider.getServiceId());
		systemInfoData.setServiceName(serviceInfoProvider.getServiceName());
//		systemInfoData.setServiceType(serviceInfoProvider.getServiceType());
		getCpuUsage().ifPresent(t -> systemInfoData.setCpuUsage(t));
		getMemoryUsage().ifPresent(t -> systemInfoData.setMemoryUsage(t));
		getDiscSpaceUsage().ifPresent(t -> systemInfoData.setDiscUsage(t));
		getCpuCount().ifPresent(t -> systemInfoData.setCpuCount(t));
		getTotalMemory().ifPresent(t -> systemInfoData.setTotalMemory(t));
		getTotalDiscSpace().ifPresent(t -> systemInfoData.setTotalDiscSpace(t));

		systemInfo.setSystemData(Arrays.asList(systemInfoData));
		return systemInfo;
	}

	@Override
	public FeatureInfo getFeatureInfo() {
		FeatureInfo featuresInfo = new FeatureInfo();
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
