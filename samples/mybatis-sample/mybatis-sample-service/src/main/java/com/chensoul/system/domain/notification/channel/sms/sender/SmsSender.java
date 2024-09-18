package com.chensoul.system.domain.notification.channel.sms.sender;

import com.chensoul.system.domain.notification.channel.sms.exception.SmsException;

public interface SmsSender {

  int sendSms(String numberTo, String message) throws SmsException;

  void destroy();

}
