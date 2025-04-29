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
package org.thingsboard.queue.provider;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.thingsboard.queue.common.TbProtoQueueMsg;
import org.thingsboard.queue.domain.TbQueueCoreSettings;
import org.thingsboard.queue.msg.ToCoreMsg;
import org.thingsboard.queue.msg.TopicService;
import org.thingsboard.queue.spi.TbQueueAdmin;
import org.thingsboard.queue.spi.TbQueueConsumer;
import org.thingsboard.queue.spi.TbQueueProducer;
import org.thingsboard.queue.sqs.TbAwsSqsAdmin;
import org.thingsboard.queue.sqs.TbAwsSqsConsumerTemplate;
import org.thingsboard.queue.sqs.TbAwsSqsProducerTemplate;
import org.thingsboard.queue.sqs.TbAwsSqsQueueAttributes;
import org.thingsboard.queue.sqs.TbAwsSqsSettings;

@Component
@ConditionalOnExpression("'${queue.type:null}'=='aws-sqs' && '${service.type:null}'=='monolith'")
public class AwsSqsMonolithQueueFactory implements TbCoreQueueFactory {
	private final TopicService topicService;
	private final TbQueueCoreSettings coreSettings;
	private final TbAwsSqsSettings sqsSettings;
	private final TbQueueAdmin coreAdmin;

	public AwsSqsMonolithQueueFactory(TopicService topicService, TbQueueCoreSettings coreSettings,
									  TbAwsSqsSettings sqsSettings,
									  TbAwsSqsQueueAttributes sqsQueueAttributes) {
		this.topicService = topicService;
		this.coreSettings = coreSettings;
		this.sqsSettings = sqsSettings;

		this.coreAdmin = new TbAwsSqsAdmin(sqsSettings, sqsQueueAttributes.getCoreAttributes());
	}

	@Override
	public TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> createTbCoreMsgProducer() {
		return new TbAwsSqsProducerTemplate<>(coreAdmin, sqsSettings, topicService.buildTopicName(coreSettings.getTopic()));
	}

	@Override
	public TbQueueConsumer<TbProtoQueueMsg<ToCoreMsg>> createToCoreMsgConsumer() {
		return new TbAwsSqsConsumerTemplate<>(coreAdmin, sqsSettings, topicService.buildTopicName(coreSettings.getTopic()),
			msg -> new TbProtoQueueMsg<>(msg.getKey(), new ToCoreMsg(String.valueOf(msg.getData()))));
	}

	@PreDestroy
	private void destroy() {
		if (coreAdmin != null) {
			coreAdmin.destroy();
		}
	}
}
