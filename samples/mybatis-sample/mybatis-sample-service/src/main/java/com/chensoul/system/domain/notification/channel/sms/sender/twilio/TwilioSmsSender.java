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
