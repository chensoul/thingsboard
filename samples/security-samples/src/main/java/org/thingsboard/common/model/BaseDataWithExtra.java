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
package org.thingsboard.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract class BaseDataWithExtra<I extends Serializable> extends BaseData<I> implements HasExtra {

	protected transient JsonNode extra;

	@JsonIgnore
	protected byte[] extraBytes;

	@Override
	public JsonNode getExtra() {
		return getJson(() -> extra, () -> extraBytes);
	}

	public void setExtra(JsonNode extra) {
		setJson(extra, json -> this.extra = json, bytes -> this.extraBytes = bytes);
	}

	public static JsonNode getJson(Supplier<JsonNode> jsonData, Supplier<byte[]> binaryData) {
		JsonNode json = jsonData.get();
		if (json != null) {
			return json;
		} else {
			byte[] data = binaryData.get();
			if (data != null) {
				try {
					return mapper.readTree(new ByteArrayInputStream(data));
				} catch (IOException e) {
					log.warn("Can't deserialize json data: ", e);
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public static void setJson(JsonNode json, Consumer<JsonNode> jsonConsumer, Consumer<byte[]> bytesConsumer) {
		jsonConsumer.accept(json);
		try {
			bytesConsumer.accept(mapper.writeValueAsBytes(json));
		} catch (JsonProcessingException e) {
			log.warn("Can't serialize json data: ", e);
		}
	}
}
