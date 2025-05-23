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
package org.thingsboard.data.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.thingsboard.common.model.ToData;

public interface BaseEntity<D, I> extends ToData<D> {

	I getId();

	void setId(I id);

	Long getCreatedTime();

	void setCreatedTime(Long createdTime);

	Long getUpdatedTime();

	void setUpdatedTime(Long createdTime);

	default String listToString(List<?> list) {
		if (list != null) {
			return StringUtils.join(list, ',');
		} else {
			return "";
		}
	}

	default <E> List<E> listFromString(String string, Function<String, E> mappingFunction) {
		if (string != null) {
			return Arrays.stream(StringUtils.split(string, ','))
				.filter(StringUtils::isNotBlank)
				.map(mappingFunction).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
}
