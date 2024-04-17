package org.thingsboard.queue.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thingsboard.domain.message.ProtoQueueMsg;
import org.thingsboard.domain.message.ToTransportMsg;
import org.thingsboard.domain.message.ToUsageStatsServiceMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.queue.common.DefaultQueueRequestTemplate;
import org.thingsboard.queue.common.ServiceInfoProvider;
import org.thingsboard.queue.common.TopicService;
import org.thingsboard.queue.setting.TbQueueTransportApiSetting;
import org.thingsboard.queue.setting.TbQueueTransportNotificationSetting;
import org.thingsboard.queue.spi.QueueAdmin;
import org.thingsboard.queue.spi.QueueConsumer;
import org.thingsboard.queue.spi.QueueProducer;
import org.thingsboard.queue.spi.QueueRequestTemplate;
import org.thingsboard.transport.queue.TransportQueueFactory;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@Component
public class InMemoryTransportQueueFactory implements TransportQueueFactory {
	private final TbQueueTransportApiSetting transportApiSettings;
	private final TbQueueTransportNotificationSetting transportNotificationSettings;
	private final ServiceInfoProvider serviceInfoProvider;
	private final InMemoryStorage storage;
	private final TopicService topicService;
	private final QueueAdmin queueAdmin;

	@Override
	public QueueRequestTemplate<ProtoQueueMsg<TransportApiRequestMsg>, ProtoQueueMsg<TransportApiResponseMsg>> createTransportApiRequestTemplate() {
		InMemoryQueueProducer<ProtoQueueMsg<TransportApiRequestMsg>> producerTemplate =
			new InMemoryQueueProducer<>(storage, topicService.buildTopicName(transportApiSettings.getRequestsTopic()));

		InMemoryQueueConsumer<ProtoQueueMsg<TransportApiResponseMsg>> consumerTemplate =
			new InMemoryQueueConsumer<>(storage, topicService.buildTopicName(transportApiSettings.getResponsesTopic() + "." + serviceInfoProvider.getServiceId()));

		DefaultQueueRequestTemplate.DefaultQueueRequestTemplateBuilder
			<ProtoQueueMsg<TransportApiRequestMsg>, ProtoQueueMsg<TransportApiResponseMsg>> templateBuilder = DefaultQueueRequestTemplate.builder();

		templateBuilder.queueAdmin(queueAdmin);
		templateBuilder.requestTemplate(producerTemplate);
		templateBuilder.responseTemplate(consumerTemplate);
		templateBuilder.maxPendingRequests(transportApiSettings.getMaxPendingRequests());
		templateBuilder.maxRequestTimeout(transportApiSettings.getMaxRequestsTimeout());
		templateBuilder.pollInterval(transportApiSettings.getResponsePollInterval());
		return templateBuilder.build();
	}

	@Override
	public QueueConsumer<ProtoQueueMsg<ToTransportMsg>> createTransportNotificationsConsumer() {
		return new InMemoryQueueConsumer<>(storage, topicService.buildTopicName(transportNotificationSettings.getNotificationsTopic() + "." + serviceInfoProvider.getServiceId()));
	}

	@Override
	public QueueProducer<ProtoQueueMsg<ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer() {
		return null;
	}
}
