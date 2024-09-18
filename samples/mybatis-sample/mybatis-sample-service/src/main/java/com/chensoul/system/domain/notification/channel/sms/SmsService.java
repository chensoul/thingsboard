package com.chensoul.system.domain.notification.channel.sms;

public interface SmsService {

  void updateSmsConfiguration();

  void sendSms(String tenantId, Long customerId, String[] numbersTo, String message);

  void sendTestSms(TestSmsRequest testSmsRequest);

  boolean isConfigured(String tenantId);

}
