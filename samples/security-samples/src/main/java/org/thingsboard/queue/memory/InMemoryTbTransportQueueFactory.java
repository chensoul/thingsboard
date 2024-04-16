package org.thingsboard.queue.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.message.TbProtoQueueMsg;
import org.thingsboard.domain.message.ToTransportMsg;
import org.thingsboard.domain.message.ToUsageStatsServiceMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.queue.common.DefaultTbQueueRequestTemplate;
import org.thingsboard.queue.common.ServiceInfoProvider;
import org.thingsboard.queue.common.TopicService;
import org.thingsboard.queue.setting.TbQueueTransportApiSettings;
import org.thingsboard.queue.setting.TbQueueTransportNotificationSettings;
import org.thingsboard.queue.spi.TbQueueAdmin;
import org.thingsboard.queue.spi.TbQueueConsumer;
import org.thingsboard.queue.spi.TbQueueProducer;
import org.thingsboard.queue.spi.TbQueueRequestTemplate;
import org.thingsboard.transport.TbTransportQueueFactory;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class InMemoryTbTransportQueueFactory implements TbTransportQueueFactory {
	private final TbQueueTransportApiSettings transportApiSettings;
	private final TbQueueTransportNotificationSettings transportNotificationSettings;
	private final ServiceInfoProvider serviceInfoProvider;
	private final InMemoryStorage storage;
	private final TopicService topicService;
	private final TbQueueAdmin queueAdmin;

	@Override
	public TbQueueRequestTemplate<TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> createTransportApiRequestTemplate() {
		InMemoryTbQueueProducer<TbProtoQueueMsg<TransportApiRequestMsg>> producerTemplate =
			new InMemoryTbQueueProducer<>(storage, topicService.buildTopicName(transportApiSettings.getRequestsTopic()));

		InMemoryTbQueueConsumer<TbProtoQueueMsg<TransportApiResponseMsg>> consumerTemplate =
			new InMemoryTbQueueConsumer<>(storage, topicService.buildTopicName(transportApiSettings.getResponsesTopic() + "." + serviceInfoProvider.getServiceId()));

		DefaultTbQueueRequestTemplate.DefaultTbQueueRequestTemplateBuilder
			<TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> templateBuilder = DefaultTbQueueRequestTemplate.builder();

		templateBuilder.queueAdmin(queueAdmin);
		templateBuilder.requestTemplate(producerTemplate);
		templateBuilder.responseTemplate(consumerTemplate);
		templateBuilder.maxPendingRequests(transportApiSettings.getMaxPendingRequests());
		templateBuilder.maxRequestTimeout(transportApiSettings.getMaxRequestsTimeout());
		templateBuilder.pollInterval(transportApiSettings.getResponsePollInterval());
		return templateBuilder.build();
	}

	@Override
	public TbQueueConsumer<TbProtoQueueMsg<ToTransportMsg>> createTransportNotificationsConsumer() {
		return new InMemoryTbQueueConsumer<>(storage, topicService.buildTopicName(transportNotificationSettings.getNotificationsTopic() + "." + serviceInfoProvider.getServiceId()));
	}

	@Override
	public TbQueueProducer<TbProtoQueueMsg<ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer() {
		return null;
	}
}
