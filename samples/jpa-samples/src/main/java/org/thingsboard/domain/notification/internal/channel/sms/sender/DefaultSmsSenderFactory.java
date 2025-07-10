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
package org.thingsboard.domain.notification.internal.channel.sms.sender;

import org.springframework.stereotype.Component;
import org.thingsboard.domain.notification.internal.channel.sms.sender.aws.AwsSnsSmsProviderConfiguration;
import org.thingsboard.domain.notification.internal.channel.sms.sender.smpp.SmppSmsProviderConfiguration;
import org.thingsboard.domain.notification.internal.channel.sms.sender.twilio.TwilioSmsProviderConfiguration;
import org.thingsboard.domain.notification.internal.channel.sms.sender.aws.AwsSmsSender;
import org.thingsboard.domain.notification.internal.channel.sms.sender.smpp.SmppSmsSender;
import org.thingsboard.domain.notification.internal.channel.sms.sender.twilio.TwilioSmsSender;

@Component
public class DefaultSmsSenderFactory implements SmsSenderFactory {

	@Override
	public SmsSender createSmsSender(SmsProviderConfiguration config) {
		switch (config.getType()) {
			case AWS_SNS:
				return new AwsSmsSender((AwsSnsSmsProviderConfiguration) config);
			case TWILIO:
				return new TwilioSmsSender((TwilioSmsProviderConfiguration) config);
			case SMPP:
				return new SmppSmsSender((SmppSmsProviderConfiguration) config);
			default:
				throw new RuntimeException("Unknown SMS provider type " + config.getType());
		}
	}
}
