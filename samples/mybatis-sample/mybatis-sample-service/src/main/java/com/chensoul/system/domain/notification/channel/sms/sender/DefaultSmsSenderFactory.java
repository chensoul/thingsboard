package com.chensoul.system.domain.notification.channel.sms.sender;

import com.chensoul.system.domain.notification.channel.sms.sender.aws.AwsSmsSender;
import com.chensoul.system.domain.notification.channel.sms.sender.aws.AwsSnsSmsProviderConfiguration;
import com.chensoul.system.domain.notification.channel.sms.sender.smpp.SmppSmsProviderConfiguration;
import com.chensoul.system.domain.notification.channel.sms.sender.smpp.SmppSmsSender;
import com.chensoul.system.domain.notification.channel.sms.sender.twilio.TwilioSmsProviderConfiguration;
import com.chensoul.system.domain.notification.channel.sms.sender.twilio.TwilioSmsSender;
import org.springframework.stereotype.Component;

@Component
public class DefaultSmsSenderFactory implements SmsSenderFactory {

  @Override
  public SmsSender createSmsSender(SmsProviderConfiguration config) {
    switch (config.getType()) {
      case AWS_SNS:
        return new AwsSmsSender((AwsSnsSmsProviderConfiguration) config);
      case TWILIO:
        return new TwilioSmsSender((TwilioSmsProviderConfiguration) config);
      case SMPP:
        return new SmppSmsSender((SmppSmsProviderConfiguration) config);
      default:
        throw new RuntimeException("Unknown SMS provider type " + config.getType());
    }
  }
}
