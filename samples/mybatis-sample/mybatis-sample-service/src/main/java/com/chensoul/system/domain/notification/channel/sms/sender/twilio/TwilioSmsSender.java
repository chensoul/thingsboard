package com.chensoul.system.domain.notification.channel.sms.sender.twilio;

import com.chensoul.system.domain.notification.channel.sms.exception.SmsException;
import com.chensoul.system.domain.notification.channel.sms.exception.SmsParseException;
import com.chensoul.system.domain.notification.channel.sms.exception.SmsSendException;
import com.chensoul.system.domain.notification.channel.sms.sender.AbstractSmsSender;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class TwilioSmsSender extends AbstractSmsSender {

  private static final Pattern PHONE_NUMBERS_SID_MESSAGE_SERVICE_SID = Pattern.compile("^(PN|MG).*$");

  //	private TwilioRestClient twilioRestClient;
  private String numberFrom;

  public TwilioSmsSender(TwilioSmsProviderConfiguration config) {
    if (StringUtils.isEmpty(config.getAccountSid()) || StringUtils.isEmpty(config.getAccountToken()) || StringUtils.isEmpty(config.getNumberFrom())) {
      throw new IllegalArgumentException("Invalid twilio sms provider configuration: accountSid, accountToken and numberFrom should be specified!");
    }
    this.numberFrom = this.validatePhoneTwilioNumber(config.getNumberFrom());
//		this.twilioRestClient = new TwilioRestClient.Builder(config.getAccountSid(), config.getAccountToken()).build();
  }

  private String validatePhoneTwilioNumber(String phoneNumber) throws SmsParseException {
    phoneNumber = phoneNumber.trim();
    if (!E_164_PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches() && !PHONE_NUMBERS_SID_MESSAGE_SERVICE_SID.matcher(phoneNumber).matches()) {
      throw new SmsParseException("Invalid phone number format. Phone number must be in E.164 format/Phone Number's SID/Messaging Service SID.");
    }
    return phoneNumber;
  }

  @Override
  public int sendSms(String numberTo, String message) throws SmsException {
    numberTo = this.validatePhoneNumber(numberTo);
    message = this.prepareMessage(message);
    try {
//			String numSegments = Message.creator(new PhoneNumber(numberTo), new PhoneNumber(this.numberFrom), message).create(this.twilioRestClient).getNumSegments();
//			return Integer.valueOf(numSegments);
      return 1;
    } catch (Exception e) {
      throw new SmsSendException("Failed to send SMS message - " + e.getMessage(), e);
    }
  }

  @Override
  public void destroy() {

  }
}
