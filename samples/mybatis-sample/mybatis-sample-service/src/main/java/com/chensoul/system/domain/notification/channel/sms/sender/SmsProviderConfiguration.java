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

import com.chensoul.system.domain.notification.channel.sms.sender.aws.AwsSnsSmsProviderConfiguration;
import com.chensoul.system.domain.notification.channel.sms.sender.smpp.SmppSmsProviderConfiguration;
import com.chensoul.system.domain.notification.channel.sms.sender.twilio.TwilioSmsProviderConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AwsSnsSmsProviderConfiguration.class, name = "AWS_SNS"),
        @JsonSubTypes.Type(value = TwilioSmsProviderConfiguration.class, name = "TWILIO"),
        @JsonSubTypes.Type(value = SmppSmsProviderConfiguration.class, name = "SMPP")
})
public interface SmsProviderConfiguration {

  @JsonIgnore
  SmsProviderType getType();

}
