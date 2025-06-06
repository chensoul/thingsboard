/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
