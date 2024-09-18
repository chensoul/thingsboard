package com.chensoul.system.domain.notification.channel.sms.sender.aws;

import com.chensoul.system.domain.notification.channel.sms.exception.SmsException;
import com.chensoul.system.domain.notification.channel.sms.sender.AbstractSmsSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AwsSmsSender extends AbstractSmsSender {

//	private static final Map<String, MessageAttributeValue> SMS_ATTRIBUTES = new HashMap<>();
//
//	static {
//		SMS_ATTRIBUTES.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
//			.withStringValue("Transactional")
//			.withDataType("String"));
//	}

//	private AmazonSNS snsClient;

  public AwsSmsSender(AwsSnsSmsProviderConfiguration config) {
//		if (StringUtils.isEmpty(config.getAccessKeyId()) || StringUtils.isEmpty(config.getSecretAccessKey()) || StringUtils.isEmpty(config.getRegion())) {
//			throw new IllegalArgumentException("Invalid AWS sms provider configuration: aws accessKeyId, aws secretAccessKey and aws region should be specified!");
//		}
//		AWSCredentials awsCredentials = new BasicAWSCredentials(config.getAccessKeyId(), config.getSecretAccessKey());
//		AWSStaticCredentialsProvider credProvider = new AWSStaticCredentialsProvider(awsCredentials);
//		this.snsClient = AmazonSNSClient.builder()
//			.withCredentials(credProvider)
//			.withRegion(config.getRegion())
//			.build();
  }

  @Override
  public int sendSms(String numberTo, String message) throws SmsException {
    numberTo = this.validatePhoneNumber(numberTo);
    message = this.prepareMessage(message);

    //...
    return this.countMessageSegments(message);
  }

  @Override
  public void destroy() {
//		if (this.snsClient != null) {
//			try {
//				this.snsClient.shutdown();
//			} catch (Exception e) {
//				log.error("Failed to shutdown SNS client during destroy()", e);
//			}
//		}
  }
}
