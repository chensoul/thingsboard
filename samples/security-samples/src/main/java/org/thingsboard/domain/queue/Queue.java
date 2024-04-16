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
package org.thingsboard.domain.queue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import lombok.Data;
import org.thingsboard.common.model.BaseDataWithExtra;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

@Data
public class Queue extends BaseDataWithExtra<Long> implements HasName, HasTenantId {
	private String tenantId;
	@NoXss
	@Length
	private String name;
	@NoXss
	@Length
	private String topic;
	private int pollInterval;
	private int partitions;
	private boolean consumerPerPartition;
	private long packProcessingTimeout;
	private SubmitStrategy submitStrategy;
	private ProcessingStrategy processingStrategy;

	public Queue() {
	}

	@JsonIgnore
	public String getCustomProperties() {
		return Optional.ofNullable(getExtra())
			.map(info -> info.get("customProperties"))
			.filter(JsonNode::isTextual).map(JsonNode::asText).orElse(null);
	}

}
