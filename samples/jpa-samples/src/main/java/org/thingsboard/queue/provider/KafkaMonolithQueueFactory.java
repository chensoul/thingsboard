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
import org.thingsboard.queue.kafka.TbKafkaAdmin;
import org.thingsboard.queue.kafka.TbKafkaConsumerStatsService;
import org.thingsboard.queue.kafka.TbKafkaConsumerTemplate;
import org.thingsboard.queue.kafka.TbKafkaProducerTemplate;
import org.thingsboard.queue.kafka.TbKafkaSettings;
import org.thingsboard.queue.kafka.TbKafkaTopicConfigs;
import org.thingsboard.queue.msg.ToCoreMsg;
import org.thingsboard.queue.msg.TopicService;
import org.thingsboard.queue.spi.TbQueueAdmin;
import org.thingsboard.queue.spi.TbQueueConsumer;
import org.thingsboard.queue.spi.TbQueueProducer;

@Component
//@ConditionalOnExpression("'${queue.type:null}'=='kafka' && '${service.type:null}'=='monolith'")
public class KafkaMonolithQueueFactory implements TbCoreQueueFactory {

	private final TopicService topicService;
	private final TbKafkaSettings kafkaSettings;
	private final TbQueueCoreSettings coreSettings;
	private final TbKafkaConsumerStatsService consumerStatsService;
	private final TbQueueAdmin coreAdmin;

	public KafkaMonolithQueueFactory(TopicService topicService, TbKafkaSettings kafkaSettings,
									 TbQueueCoreSettings coreSettings,
									 TbKafkaConsumerStatsService consumerStatsService,
									 TbKafkaTopicConfigs kafkaTopicConfigs) {
		this.topicService = topicService;
		this.kafkaSettings = kafkaSettings;
		this.consumerStatsService = consumerStatsService;
		this.coreSettings = coreSettings;

		this.coreAdmin = new TbKafkaAdmin(kafkaSettings, kafkaTopicConfigs.getCoreConfigs());
	}

	@Override
	public TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> createTbCoreMsgProducer() {
		TbKafkaProducerTemplate.TbKafkaProducerTemplateBuilder<TbProtoQueueMsg<ToCoreMsg>> requestBuilder = TbKafkaProducerTemplate.builder();
		requestBuilder.settings(kafkaSettings);
		requestBuilder.clientId("monolith-core-producer-" + 1);
		requestBuilder.defaultTopic(topicService.buildTopicName(coreSettings.getTopic()));
		requestBuilder.admin(coreAdmin);
		return requestBuilder.build();
	}

	@Override
	public TbQueueConsumer<TbProtoQueueMsg<ToCoreMsg>> createToCoreMsgConsumer() {
		TbKafkaConsumerTemplate.TbKafkaConsumerTemplateBuilder<TbProtoQueueMsg<ToCoreMsg>> consumerBuilder = TbKafkaConsumerTemplate.builder();
		consumerBuilder.settings(kafkaSettings);
		consumerBuilder.topic(topicService.buildTopicName(coreSettings.getTopic()));
		consumerBuilder.clientId("monolith-core-consumer-" + 1);
		consumerBuilder.groupId(topicService.buildTopicName("monolith-core-consumer"));
		consumerBuilder.decoder(msg -> new TbProtoQueueMsg<>(msg.getKey(), new ToCoreMsg(String.valueOf(msg.getData()))));
		consumerBuilder.admin(coreAdmin);
		consumerBuilder.statsService(consumerStatsService);
		return consumerBuilder.build();
	}

	@PreDestroy
	private void destroy() {
		if (coreAdmin != null) {
			coreAdmin.destroy();
		}
	}
}
