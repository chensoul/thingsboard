package org.thingsboard.queue.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.message.TbProtoQueueMsg;
import org.thingsboard.domain.message.ToUsageStatsServiceMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.queue.common.TopicService;
import org.thingsboard.queue.memory.InMemoryStorage;
import org.thingsboard.queue.memory.InMemoryTbQueueConsumer;
import org.thingsboard.queue.memory.InMemoryTbQueueProducer;
import org.thingsboard.queue.setting.TbQueueTransportApiSettings;
import org.thingsboard.queue.spi.TbQueueConsumer;
import org.thingsboard.queue.spi.TbQueueProducer;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class InMemoryQueueFactory implements TbCoreQueueFactory {
	private final TopicService topicService;
	private final TbQueueTransportApiSettings transportApiSettings;
	private final InMemoryStorage storage;

	@Override
	public TbQueueConsumer<TbProtoQueueMsg<TransportApiRequestMsg>> createTransportApiRequestConsumer() {
		return new InMemoryTbQueueConsumer<>(storage, topicService.buildTopicName(transportApiSettings.getRequestsTopic()));
	}

	@Override
	public TbQueueProducer<TbProtoQueueMsg<TransportApiResponseMsg>> createTransportApiResponseProducer() {
		return new InMemoryTbQueueProducer<>(storage, topicService.buildTopicName(transportApiSettings.getResponsesTopic()));
	}

	@Override
	public TbQueueProducer<TbProtoQueueMsg<ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer() {
		return null;
	}
}
