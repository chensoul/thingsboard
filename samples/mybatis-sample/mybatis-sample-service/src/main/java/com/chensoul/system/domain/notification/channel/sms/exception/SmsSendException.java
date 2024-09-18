package com.chensoul.system.domain.notification.channel.sms.exception;

public class SmsSendException extends SmsException {

  public SmsSendException(String msg) {
    super(msg);
  }

  public SmsSendException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
