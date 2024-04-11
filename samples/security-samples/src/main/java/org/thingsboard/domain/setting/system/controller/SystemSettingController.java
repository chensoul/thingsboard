package org.thingsboard.domain.setting.system.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.domain.setting.system.model.SystemSetting;
import org.thingsboard.domain.setting.system.model.SystemSettingType;
import org.thingsboard.domain.setting.system.service.SystemSettingService;
import org.thingsboard.server.mail.MailService;
import static org.thingsboard.server.security.SecurityUtils.getCurrentUser;
import org.thingsboard.domain.setting.jwt.JwtSettingService;
import org.thingsboard.server.security.jwt.token.JwtPair;
import org.thingsboard.domain.setting.jwt.JwtSetting;
import org.thingsboard.domain.setting.security.SecuritySetting;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;
import org.thingsboard.server.security.jwt.JwtTokenFactory;
import org.thingsboard.domain.setting.security.SecuritySettingService;
import org.thingsboard.server.sms.SmsService;
import org.thingsboard.server.sms.TestSmsRequest;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@Slf4j
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SystemSettingController {
	private final MailService mailService;
	private final SystemSettingService systemSettingService;
	private final SecuritySettingService securitySettingService;
	private final JwtSettingService jwtSettingService;
	private final JwtTokenFactory tokenFactory;
	private final SmsService smsService;

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@GetMapping(value = "/settings/{type}")
	public SystemSetting getAdminSettings(@PathVariable("type") SystemSettingType type) {
		SystemSetting systemSetting = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, type);
		if (systemSetting != null && systemSetting.getType().equals(SystemSettingType.MAIL)) {
			((ObjectNode) systemSetting.getExtra()).remove("password");
			((ObjectNode) systemSetting.getExtra()).remove("refreshToken");
		}
		return systemSetting;
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@PostMapping(value = "/settings")
	public SystemSetting saveAdminSettings(@RequestBody SystemSetting systemSetting) {
		systemSetting = systemSettingService.saveSystemSetting(SYS_TENANT_ID, systemSetting);
		if (systemSetting.getType().equals(SystemSettingType.MAIL)) {
			mailService.updateMailConfiguration();
			((ObjectNode) systemSetting.getExtra()).remove("password");
			((ObjectNode) systemSetting.getExtra()).remove("refreshToken");
		} else if (systemSetting.getType().equals(SystemSettingType.SMS)) {
			smsService.updateSmsConfiguration();
		}
		return systemSetting;
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@GetMapping(value = "/securitySettings")
	public SecuritySetting getSecuritySettings() {
		return securitySettingService.getSecuritySettings();
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@PostMapping(value = "/securitySettings")
	public SecuritySetting saveSecuritySettings(@RequestBody SecuritySetting securitySetting) {
		return securitySettingService.saveSecuritySettings(securitySetting);
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/jwtSetting", method = RequestMethod.GET)
	public JwtSetting getJwtSettings() {
		return jwtSettingService.getJwtSetting();
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/jwtSetting", method = RequestMethod.POST)
	public JwtPair saveJwtSetting(@RequestBody JwtSetting jwtSetting) {
		jwtSettingService.saveJwtSetting(jwtSetting);
		return tokenFactory.createTokenPair(getCurrentUser());
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/settings/testMail", method = RequestMethod.POST)
	public void sendTestMail(@RequestBody SystemSetting systemSetting) {
		if (systemSetting.getType().equals(SystemSettingType.MAIL)) {
			if (systemSetting.getExtra().has("enableOauth2") && systemSetting.getExtra().get("enableOauth2").asBoolean()) {
				SystemSetting mailSettings = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.MAIL);
				JsonNode refreshToken = mailSettings.getExtra().get("refreshToken");
				if (refreshToken == null) {
					throw new ThingsboardException("Refresh token was not generated. Please, generate refresh token.", ThingsboardErrorCode.GENERAL);
				}
				ObjectNode settings = (ObjectNode) systemSetting.getExtra();
				settings.put("refreshToken", refreshToken.asText());
			} else {
				if (!systemSetting.getExtra().has("password")) {
					SystemSetting mailSettings = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.MAIL);
					((ObjectNode) systemSetting.getExtra()).put("password", mailSettings.getExtra().get("password").asText());
				}
			}
			String email = getCurrentUser().getEmail();
			mailService.sendTestMail(systemSetting.getExtra(), email);
		}
	}

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/settings/testSms", method = RequestMethod.POST)
	public void sendTestSms(
		@RequestBody TestSmsRequest testSmsRequest) {
		try {
			smsService.sendTestSms(testSmsRequest);
		} catch (ThingsboardException e) {
			throw e;
		}
	}
}
