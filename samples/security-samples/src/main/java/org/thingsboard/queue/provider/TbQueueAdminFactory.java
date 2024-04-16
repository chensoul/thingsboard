/**
 * Copyright © 2016-2024 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.queue.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thingsboard.queue.spi.TbQueueAdmin;

@Configuration
public class TbQueueAdminFactory {
	@Bean
	public TbQueueAdmin createInMemoryAdmin() {
		return new TbQueueAdmin() {

			@Override
			public void createTopicIfNotExists(String topic, String properties) {
			}

			@Override
			public void deleteTopic(String topic) {
			}

			@Override
			public void destroy() {
			}
		};
	}
}
