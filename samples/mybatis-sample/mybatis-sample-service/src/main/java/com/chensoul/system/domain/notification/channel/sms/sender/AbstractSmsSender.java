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
package com.chensoul.system.domain.notification.channel.sms.sender;

import com.chensoul.system.domain.notification.channel.sms.exception.SmsParseException;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSmsSender implements SmsSender {

  protected static final Pattern E_164_PHONE_NUMBER_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

  private static final int MAX_SMS_MESSAGE_LENGTH = 1600;
  private static final int MAX_SMS_SEGMENT_LENGTH = 70;

  protected String validatePhoneNumber(String phoneNumber) throws SmsParseException {
    phoneNumber = phoneNumber.trim();
    if (!E_164_PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()) {
      throw new SmsParseException("Invalid phone number format. Phone number must be in E.164 format.");
    }
    return phoneNumber;
  }

  protected String prepareMessage(String message) {
    message = message.replaceAll("^\"|\"$", "").replaceAll("\\\\n", "\n");
    if (message.length() > MAX_SMS_MESSAGE_LENGTH) {
      log.warn("SMS message exceeds maximum symbols length and will be truncated");
      message = message.substring(0, MAX_SMS_MESSAGE_LENGTH);
    }
    return message;
  }

  protected int countMessageSegments(String message) {
    return (int) Math.ceil((double) message.length() / (double) MAX_SMS_SEGMENT_LENGTH);
  }

}
