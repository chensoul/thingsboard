package com.chensoul.system.domain.notification.channel.sms;

import com.chensoul.system.domain.notification.channel.sms.sender.SmsProviderConfiguration;
import lombok.Data;

@Data
public class TestSmsRequest {
  private SmsProviderConfiguration providerConfiguration;
  private String numberTo;
  private String message;

}
