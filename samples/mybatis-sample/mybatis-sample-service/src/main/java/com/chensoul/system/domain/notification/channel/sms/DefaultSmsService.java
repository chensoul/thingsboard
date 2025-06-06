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
package com.chensoul.system.domain.notification.channel.sms;

import static com.chensoul.system.DataConstants.SYS_TENANT_ID;
import com.chensoul.system.domain.notification.channel.sms.sender.SmsProviderConfiguration;
import com.chensoul.system.domain.notification.channel.sms.sender.SmsSender;
import com.chensoul.system.domain.notification.channel.sms.sender.SmsSenderFactory;
import com.chensoul.system.domain.setting.domain.SystemSetting;
import com.chensoul.system.domain.setting.domain.SystemSettingType;
import com.chensoul.system.domain.setting.mybatis.SystemSettingDao;
import com.chensoul.exception.BusinessException;
import com.chensoul.json.JacksonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultSmsService implements SmsService {
  private final SmsSenderFactory smsSenderFactory;
  private final SystemSettingDao systemSettingDao;
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
    SystemSetting systemSetting = systemSettingDao.findByType(SYS_TENANT_ID, SystemSettingType.SMS);
    if (systemSetting != null) {
      try {
        JsonNode jsonConfig = systemSetting.getExtra();
        SmsProviderConfiguration configuration = JacksonUtils.convertValue(jsonConfig, SmsProviderConfiguration.class);
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
      throw new BusinessException("Unable to send SMS: no SMS provider configured!");
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

  private BusinessException handleException(Exception exception) {
    String message;
    if (exception instanceof NestedRuntimeException) {
      message = ((NestedRuntimeException) exception).getMostSpecificCause().getMessage();
    } else {
      message = exception.getMessage();
    }
    log.warn("Unable to send SMS: {}", message);
    return new BusinessException(String.format("Unable to send SMS: %s", message));
  }
}
