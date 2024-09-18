package com.chensoul.system.domain.notification.channel.sms.sender.twilio;

import com.chensoul.system.domain.notification.channel.sms.sender.SmsProviderConfiguration;
import com.chensoul.system.domain.notification.channel.sms.sender.SmsProviderType;
import lombok.Data;

@Data
public class TwilioSmsProviderConfiguration implements SmsProviderConfiguration {

  private String accountSid;
  private String accountToken;
  private String numberFrom;

  @Override
  public SmsProviderType getType() {
    return SmsProviderType.TWILIO;
  }

}
