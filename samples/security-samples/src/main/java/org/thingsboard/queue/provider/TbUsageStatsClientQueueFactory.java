package org.thingsboard.queue.provider;

import org.thingsboard.domain.message.TbProtoQueueMsg;
import org.thingsboard.domain.message.ToUsageStatsServiceMsg;
import org.thingsboard.queue.spi.TbQueueProducer;

public interface TbUsageStatsClientQueueFactory {

    TbQueueProducer<TbProtoQueueMsg<ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer();

}
