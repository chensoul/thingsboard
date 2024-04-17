package org.thingsboard.queue.provider;

import org.thingsboard.domain.message.ProtoQueueMsg;
import org.thingsboard.domain.message.ToUsageStatsServiceMsg;
import org.thingsboard.queue.spi.QueueProducer;

public interface UsageStatsClientQueueFactory {

    QueueProducer<ProtoQueueMsg<ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer();

}
