package org.thingsboard;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.queue.common.TbProtoQueueMsg;
import org.thingsboard.queue.msg.ToCoreMsg;
import org.thingsboard.queue.msg.TopicPartitionInfo;
import org.thingsboard.queue.provider.TbQueueProducerProvider;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/queue")
public class QueueTestController {
	private final TbQueueProducerProvider tbQueueProducerProvider;

	@PostConstruct
	public void test() {
		TbProtoQueueMsg tbProtoQueueMsg = new TbProtoQueueMsg(UUID.randomUUID(), new ToCoreMsg("test"));
		tbQueueProducerProvider.getTbCoreMsgProducer().send(TopicPartitionInfo.builder().topic("tb_core").build(), tbProtoQueueMsg, null);
	}
}
