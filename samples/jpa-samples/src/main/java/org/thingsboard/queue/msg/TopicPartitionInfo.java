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
package org.thingsboard.queue.msg;

import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
public class TopicPartitionInfo {

	private final String topic;
	private final String tenantId;
	private final Integer partition;
	@Getter
	private final String fullTopicName;
	@Getter
	private final boolean myPartition;

	@Builder
	public TopicPartitionInfo(String topic, String tenantId, Integer partition, boolean myPartition) {
		this.topic = topic;
		this.tenantId = tenantId;
		this.partition = partition;
		this.myPartition = myPartition;
		String tmp = topic;
		if (tenantId != null) {
			tmp += ".isolated." + tenantId;
		}
		if (partition != null) {
			tmp += "." + partition;
		}
		this.fullTopicName = tmp;
	}

	public TopicPartitionInfo newByTopic(String topic) {
		return new TopicPartitionInfo(topic, this.tenantId, this.partition, this.myPartition);
	}

	public String getTopic() {
		return topic;
	}

	public Optional<String> getTenantId() {
		return Optional.ofNullable(tenantId);
	}

	public Optional<Integer> getPartition() {
		return Optional.ofNullable(partition);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TopicPartitionInfo that = (TopicPartitionInfo) o;
		return topic.equals(that.topic) &&
			   Objects.equals(tenantId, that.tenantId) &&
			   Objects.equals(partition, that.partition) &&
			   fullTopicName.equals(that.fullTopicName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fullTopicName);
	}
}
