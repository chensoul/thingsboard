package org.thingsboard.queue.provider;

import org.thingsboard.domain.message.ProtoQueueMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.queue.spi.QueueConsumer;
import org.thingsboard.queue.spi.QueueProducer;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface CoreQueueFactory extends UsageStatsClientQueueFactory {
	QueueConsumer<ProtoQueueMsg<TransportApiRequestMsg>> createTransportApiRequestConsumer();

	QueueProducer<ProtoQueueMsg<TransportApiResponseMsg>> createTransportApiResponseProducer();
}
