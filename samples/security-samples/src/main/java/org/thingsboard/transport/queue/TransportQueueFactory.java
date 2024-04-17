package org.thingsboard.transport.queue;

import org.thingsboard.domain.message.ProtoQueueMsg;
import org.thingsboard.domain.message.ToTransportMsg;
import org.thingsboard.domain.message.TransportApiRequestMsg;
import org.thingsboard.domain.message.TransportApiResponseMsg;
import org.thingsboard.queue.provider.UsageStatsClientQueueFactory;
import org.thingsboard.queue.spi.QueueConsumer;
import org.thingsboard.queue.spi.QueueRequestTemplate;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public interface TransportQueueFactory extends UsageStatsClientQueueFactory {
	QueueRequestTemplate<ProtoQueueMsg<TransportApiRequestMsg>, ProtoQueueMsg<TransportApiResponseMsg>> createTransportApiRequestTemplate();

	QueueConsumer<ProtoQueueMsg<ToTransportMsg>> createTransportNotificationsConsumer();
}
