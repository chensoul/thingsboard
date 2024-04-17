package org.thingsboard.queue.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.message.ProtoQueueMsg;
import org.thingsboard.domain.message.ToUsageStatsServiceMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.queue.common.TopicService;
import org.thingsboard.queue.memory.InMemoryStorage;
import org.thingsboard.queue.memory.InMemoryQueueConsumer;
import org.thingsboard.queue.memory.InMemoryQueueProducer;
import org.thingsboard.queue.setting.TbQueueTransportApiSetting;
import org.thingsboard.queue.spi.QueueConsumer;
import org.thingsboard.queue.spi.QueueProducer;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class InMemoryQueueFactory implements CoreQueueFactory {
	private final TopicService topicService;
	private final TbQueueTransportApiSetting transportApiSettings;
	private final InMemoryStorage storage;

	@Override
	public QueueConsumer<ProtoQueueMsg<TransportApiRequestMsg>> createTransportApiRequestConsumer() {
		return new InMemoryQueueConsumer<>(storage, topicService.buildTopicName(transportApiSettings.getRequestsTopic()));
	}

	@Override
	public QueueProducer<ProtoQueueMsg<TransportApiResponseMsg>> createTransportApiResponseProducer() {
		return new InMemoryQueueProducer<>(storage, topicService.buildTopicName(transportApiSettings.getResponsesTopic()));
	}

	@Override
	public QueueProducer<ProtoQueueMsg<ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer() {
		return null;
	}
}
