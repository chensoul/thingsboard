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
package org.thingsboard.common.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.thingsboard.common.model.ToData;

public abstract class DaoUtil {

	private DaoUtil() {
	}

	public static <T> T getData(ToData<T> data) {
		T object = null;
		if (data != null) {
			object = data.toData();
		}
		return object;
	}

	public static <T> List<T> convertDataList(Collection<? extends ToData<T>> toDataList) {
		List<T> list = Collections.emptyList();
		if (toDataList != null && !toDataList.isEmpty()) {
			list = new ArrayList<>();
			for (ToData<T> object : toDataList) {
				if (object != null) {
					list.add(object.toData());
				}
			}
		}
		return list;
	}

	public static <T> Page<T> toPageData(com.baomidou.mybatisplus.extension.plugins.pagination.Page<? extends ToData<T>> page) {
		return new PageImpl<>(DaoUtil.convertDataList(page.getRecords()));
	}
}
