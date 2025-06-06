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
package org.thingsboard.queue.kafka;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.thingsboard.queue.msg.TopicPartitionInfo;
import org.thingsboard.queue.spi.TbQueueAdmin;
import org.thingsboard.queue.spi.TbQueueCallback;
import org.thingsboard.queue.spi.TbQueueMsg;
import org.thingsboard.queue.spi.TbQueueProducer;

/**
 * Created by ashvayka on 24.09.18.
 */
@Slf4j
public class TbKafkaProducerTemplate<T extends TbQueueMsg> implements TbQueueProducer<T> {

	private final KafkaProducer<String, byte[]> producer;

	@Getter
	private final String defaultTopic;

	@Getter
	private final TbKafkaSettings settings;

	private final TbQueueAdmin admin;

	private final Set<TopicPartitionInfo> topics;

	@Getter
	private final String clientId;

	@Builder
	private TbKafkaProducerTemplate(TbKafkaSettings settings, String defaultTopic, String clientId, TbQueueAdmin admin) {
		Properties props = settings.toProducerProps();

		this.clientId = Objects.requireNonNull(clientId, "Kafka producer client.id is null");
		if (!StringUtils.isEmpty(clientId)) {
			props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
		}
		this.settings = settings;

		this.producer = new KafkaProducer<>(props);
		this.defaultTopic = defaultTopic;
		this.admin = admin;
		topics = ConcurrentHashMap.newKeySet();
	}

	@Override
	public void init() {
	}

	void addAnalyticHeaders(List<Header> headers) {
		headers.add(new RecordHeader("_producerId", getClientId().getBytes(StandardCharsets.UTF_8)));
		headers.add(new RecordHeader("_threadName", Thread.currentThread().getName().getBytes(StandardCharsets.UTF_8)));
		if (log.isTraceEnabled()) {
			try {
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				int maxLevel = Math.min(stackTrace.length, 20);
				for (int i = 2; i < maxLevel; i++) { // ignore two levels: getStackTrace and addAnalyticHeaders
					headers.add(new RecordHeader("_stackTrace" + i, stackTrace[i].toString().getBytes(StandardCharsets.UTF_8)));
				}
			} catch (Throwable t) {
				log.trace("Failed to add stacktrace headers in Kafka producer {}", getClientId(), t);
			}
		}
	}

	@Override
	public void send(TopicPartitionInfo tpi, T msg, TbQueueCallback callback) {
		try {
			createTopicIfNotExist(tpi);
			String key = msg.getKey().toString();
			byte[] data = msg.getData();
			ProducerRecord<String, byte[]> record;
			List<Header> headers = msg.getHeaders().getData().entrySet().stream().map(e -> new RecordHeader(e.getKey(), e.getValue())).collect(Collectors.toList());
			if (log.isDebugEnabled()) {
				addAnalyticHeaders(headers);
			}
			record = new ProducerRecord<>(tpi.getFullTopicName(), null, key, data, headers);
			producer.send(record, (metadata, exception) -> {
				if (exception == null) {
					if (callback != null) {
						callback.onSuccess(new KafkaTbQueueMsgMetadata(metadata));
					}
				} else {
					if (callback != null) {
						callback.onFailure(exception);
					} else {
						log.warn("Producer template failure: {}", exception.getMessage(), exception);
					}
				}
			});
		} catch (Exception e) {
			if (callback != null) {
				callback.onFailure(e);
			} else {
				log.warn("Producer template failure (send method wrapper): {}", e.getMessage(), e);
			}
			throw e;
		}
	}

	private void createTopicIfNotExist(TopicPartitionInfo tpi) {
		if (topics.contains(tpi)) {
			return;
		}
		admin.createTopicIfNotExists(tpi.getFullTopicName());
		topics.add(tpi);
	}

	@Override
	public void stop() {
		if (producer != null) {
			producer.close();
		}
	}
}
