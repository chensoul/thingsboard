package com.chensoul.system.domain.notification.channel.sms.sender;

public interface SmsSenderFactory {

  SmsSender createSmsSender(SmsProviderConfiguration config);

}
