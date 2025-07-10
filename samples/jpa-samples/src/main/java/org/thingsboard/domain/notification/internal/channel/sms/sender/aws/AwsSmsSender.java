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
package org.thingsboard.domain.notification.internal.channel.sms.sender.aws;

import lombok.extern.slf4j.Slf4j;
import org.thingsboard.domain.notification.internal.channel.sms.exception.SmsException;
import org.thingsboard.domain.notification.internal.channel.sms.sender.AbstractSmsSender;

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
