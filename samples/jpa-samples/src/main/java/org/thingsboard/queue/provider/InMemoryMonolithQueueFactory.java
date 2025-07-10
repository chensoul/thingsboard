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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thingsboard.queue.common.TbProtoQueueMsg;
import org.thingsboard.queue.domain.TbQueueCoreSettings;
import org.thingsboard.queue.memory.InMemoryStorage;
import org.thingsboard.queue.memory.InMemoryTbQueueConsumer;
import org.thingsboard.queue.memory.InMemoryTbQueueProducer;
import org.thingsboard.queue.msg.ToCoreMsg;
import org.thingsboard.queue.msg.TopicService;
import org.thingsboard.queue.spi.TbQueueConsumer;
import org.thingsboard.queue.spi.TbQueueProducer;

@Slf4j
//@Component
@RequiredArgsConstructor
public class InMemoryMonolithQueueFactory implements TbCoreQueueFactory {
	private final TopicService topicService;
	private final TbQueueCoreSettings coreSettings;
	private final InMemoryStorage storage;

	@Override
	public TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> createTbCoreMsgProducer() {
		return new InMemoryTbQueueProducer<>(storage, topicService.buildTopicName(coreSettings.getTopic()));
	}

	@Override
	public TbQueueConsumer<TbProtoQueueMsg<ToCoreMsg>> createToCoreMsgConsumer() {
		return new InMemoryTbQueueConsumer<>(storage, topicService.buildTopicName(coreSettings.getTopic()));
	}

	@Scheduled(fixedRateString = "${queue.in_memory.stats.print-interval-ms:60000}")
	private void printInMemoryStats() {
		storage.printStats();
	}

}
