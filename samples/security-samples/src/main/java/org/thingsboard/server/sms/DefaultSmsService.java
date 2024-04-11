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
package org.thingsboard.server.sms;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.stereotype.Service;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.setting.system.model.SystemSetting;
import org.thingsboard.domain.setting.system.model.SystemSettingType;
import org.thingsboard.domain.setting.system.service.SystemSettingService;
import static org.thingsboard.server.security.SecurityUser.SYS_TENANT_ID;
import org.thingsboard.server.sms.sender.SmsSender;
import org.thingsboard.server.sms.sender.SmsSenderFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultSmsService implements SmsService {

	private final SmsSenderFactory smsSenderFactory;
	private final SystemSettingService systemSettingService;
//	private final ApiUsageStateService apiUsageStateService;

	private SmsSender smsSender;


	@PostConstruct
	private void init() {
		updateSmsConfiguration();
	}

	@PreDestroy
	private void destroy() {
		if (this.smsSender != null) {
			this.smsSender.destroy();
		}
	}

	@Override
	public void updateSmsConfiguration() {
		SystemSetting settings = systemSettingService.findSystemSettingByType(SYS_TENANT_ID, SystemSettingType.SMS);
		if (settings != null) {
			try {
				JsonNode jsonConfig = settings.getExtra();
				SmsProviderConfiguration configuration = JacksonUtil.convertValue(jsonConfig, SmsProviderConfiguration.class);
				SmsSender newSmsSender = this.smsSenderFactory.createSmsSender(configuration);
				if (this.smsSender != null) {
					this.smsSender.destroy();
				}
				this.smsSender = newSmsSender;
			} catch (Exception e) {
				log.error("Failed to create SMS sender", e);
			}
		}
	}

	protected int sendSms(String numberTo, String message) {
		if (this.smsSender == null) {
			throw new ThingsboardException("Unable to send SMS: no SMS provider configured!", ThingsboardErrorCode.GENERAL);
		}
		return this.sendSms(this.smsSender, numberTo, message);
	}

	@Override
	public void sendSms(String tenantId, Long customerId, String[] numbersTo, String message) {
//		if (apiUsageStateService.getApiUsageState(tenantId).isSmsSendEnabled()) {
		int smsCount = 0;
		try {
			for (String numberTo : numbersTo) {
				smsCount += this.sendSms(numberTo, message);
			}
		} finally {
			if (smsCount > 0) {
//					apiUsageClient.report(tenantId, customerId, ApiUsageRecordKey.SMS_EXEC_COUNT, smsCount);
			}
		}
//		} else {
//			throw new RuntimeException("SMS sending is disabled due to API limits!");
//		}
	}

	@Override
	public void sendTestSms(TestSmsRequest testSmsRequest) {
		SmsSender testSmsSender;
		try {
			testSmsSender = this.smsSenderFactory.createSmsSender(testSmsRequest.getProviderConfiguration());
		} catch (Exception e) {
			throw handleException(e);
		}
		this.sendSms(testSmsSender, testSmsRequest.getNumberTo(), testSmsRequest.getMessage());
		testSmsSender.destroy();
	}

	@Override
	public boolean isConfigured(String tenantId) {
		return smsSender != null;
	}

	private int sendSms(SmsSender smsSender, String numberTo, String message) {
		try {
			int sentSms = smsSender.sendSms(numberTo, message);
			log.trace("Successfully sent sms to number: {}", numberTo);
			return sentSms;
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	private ThingsboardException handleException(Exception exception) {
		String message;
		if (exception instanceof NestedRuntimeException) {
			message = ((NestedRuntimeException) exception).getMostSpecificCause().getMessage();
		} else {
			message = exception.getMessage();
		}
		log.warn("Unable to send SMS: {}", message);
		return new ThingsboardException(String.format("Unable to send SMS: %s", message),
			ThingsboardErrorCode.GENERAL);
	}
}
