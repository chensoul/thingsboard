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
