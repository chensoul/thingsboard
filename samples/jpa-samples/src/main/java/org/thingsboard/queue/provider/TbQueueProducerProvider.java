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
package org.thingsboard.queue.provider;


import org.thingsboard.queue.common.TbProtoQueueMsg;
import org.thingsboard.queue.msg.ToCoreMsg;
import org.thingsboard.queue.spi.TbQueueProducer;

/**
 * Responsible for providing various Producers to other services.
 */
public interface TbQueueProducerProvider {
	/**
	 * Used to push messages to other instances of TB Core Service
	 *
	 * @return
	 */
	TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> getTbCoreMsgProducer();

}
