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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.validation.Create;

@Data
@Slf4j
public abstract class BaseDataWithExtra<I extends Serializable> extends BaseData<I> implements HasExtra {
	@NotNull(message = "Id不能为空", groups = Create.class)
	protected I id;

	protected transient JsonNode extra;

	@JsonIgnore
	@ToString.Exclude
	protected byte[] extraBytes;

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

	@Override
	public JsonNode getExtra() {
		return getJson(() -> extra, () -> extraBytes);
	}

	public void setExtra(JsonNode extra) {
		setJson(extra, json -> this.extra = json, bytes -> this.extraBytes = bytes);
	}

}
