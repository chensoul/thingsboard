package org.thingsboard.common.dao.mybatis;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.common.dao.BaseEntity;
import org.thingsboard.common.util.JacksonUtil;

@Data
public abstract class LongBaseEntity<D> implements BaseEntity<Long, D> {
	@TableId(value = "id", type = IdType.AUTO)
	protected Long id;

	@TableField(updateStrategy = FieldStrategy.NEVER)
	protected Long createdTime;

	protected Long updatedTime;

	protected JsonNode toJson(Object value) {
		if (value != null) {
			return JacksonUtil.valueToTree(value);
		} else {
			return null;
		}
	}

	protected <T> T fromJson(JsonNode json, Class<T> type) {
		return JacksonUtil.convertValue(json, type);
	}

	protected String listToString(List<?> list) {
		if (list != null) {
			return StringUtils.join(list, ',');
		} else {
			return "";
		}
	}

	protected <E> List<E> listFromString(String string, Function<String, E> mappingFunction) {
		if (string != null) {
			return Arrays.stream(StringUtils.split(string, ','))
				.filter(StringUtils::isNotBlank)
				.map(mappingFunction).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
}
