package org.thingsboard.transport;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;
import org.thingsboard.common.anntation.AfterStartUp;
import org.thingsboard.common.stats.MessagesStats;
import org.thingsboard.common.stats.StatsFactory;
import org.thingsboard.common.stats.StatsType;
import org.thingsboard.common.util.ThingsBoardExecutors;
import org.thingsboard.domain.message.TbProtoQueueMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.queue.common.DefaultTbQueueResponseTemplate;
import org.thingsboard.queue.provider.TbCoreQueueFactory;
import org.thingsboard.queue.spi.TbQueueConsumer;
import org.thingsboard.queue.spi.TbQueueProducer;
import org.thingsboard.queue.spi.TbQueueResponseTemplate;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TbCoreTransportApiService {
	private final TbCoreQueueFactory tbCoreQueueFactory;
	private final TransportApiService transportApiService;
	private final StatsFactory statsFactory;

	@Value("${queue.transport_api.max_pending_requests:10000}")
	private int maxPendingRequests;
	@Value("${queue.transport_api.max_requests_timeout:10000}")
	private long requestTimeout;
	@Value("${queue.transport_api.request_poll_interval:25}")
	private int responsePollDuration;
	@Value("${queue.transport_api.max_callback_threads:100}")
	private int maxCallbackThreads;

	private ExecutorService transportCallbackExecutor;

	private TbQueueResponseTemplate<TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> transportApiTemplate;

	@PostConstruct
	public void init() {
		this.transportCallbackExecutor = ThingsBoardExecutors.newWorkStealingPool(maxCallbackThreads, getClass());
		TbQueueProducer<TbProtoQueueMsg<TransportApiResponseMsg>> producer = tbCoreQueueFactory.createTransportApiResponseProducer();
		TbQueueConsumer<TbProtoQueueMsg<TransportApiRequestMsg>> consumer = tbCoreQueueFactory.createTransportApiRequestConsumer();

		String key = StatsType.TRANSPORT.getName();
		MessagesStats queueStats = statsFactory.createMessagesStats(key);

		DefaultTbQueueResponseTemplate.DefaultTbQueueResponseTemplateBuilder
			<TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> builder = DefaultTbQueueResponseTemplate.builder();
		builder.requestTemplate(consumer);
		builder.responseTemplate(producer);
		builder.maxPendingRequests(maxPendingRequests);
		builder.requestTimeout(requestTimeout);
		builder.pollInterval(responsePollDuration);
		builder.executor(transportCallbackExecutor);
		builder.handler(transportApiService);
		builder.stats(queueStats);
		transportApiTemplate = builder.build();
	}

	@AfterStartUp(order = AfterStartUp.REGULAR_SERVICE)
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		log.info("Received application ready event. Starting polling for events.");
		transportApiTemplate.init(transportApiService);
	}

	@PreDestroy
	public void destroy() {
		if (transportApiTemplate != null) {
			transportApiTemplate.stop();
		}
		if (transportCallbackExecutor != null) {
			transportCallbackExecutor.shutdownNow();
		}
	}
}
