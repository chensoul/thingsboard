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
package org.thingsboard.data.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.thingsboard.data.model.page.PageData;
import org.thingsboard.data.model.page.PageLink;
import org.thingsboard.data.model.page.SortOrder;
import org.thingsboard.common.model.ToData;

public abstract class DaoUtil {

	private DaoUtil() {
	}

	public static <T> PageData<T> toPageData(Page<? extends ToData<T>> page) {
		List<T> data = convertDataList(page.getContent());
		return new PageData<>(data, page.getTotalPages(), page.getTotalElements(), page.hasNext());
	}

	public static <T> Page<T> toPage(Page<? extends ToData<T>> page) {
		return new PageImpl<>(DaoUtil.convertDataList(page.getContent()));
	}

	public static Pageable toPageable(PageLink pageLink) {
		return toPageable(pageLink, true);
	}

	public static Pageable toPageable(PageLink pageLink, boolean addDefaultSorting) {
		return toPageable(pageLink, Collections.emptyMap(), addDefaultSorting);
	}

	public static Pageable toPageable(PageLink pageLink, Map<String, String> columnMap) {
		return toPageable(pageLink, columnMap, true);
	}

	public static Pageable toPageable(PageLink pageLink, Map<String, String> columnMap, boolean addDefaultSorting) {
		return PageRequest.of(pageLink.getPageNumber(), pageLink.getPageSize(), pageLink.toSort(pageLink.getSort(), columnMap, addDefaultSorting));
	}

	public static Pageable toPageable(PageLink pageLink, List<SortOrder> sortOrders) {
		return toPageable(pageLink, Collections.emptyMap(), sortOrders);
	}

	public static Pageable toPageable(PageLink pageLink, Map<String, String> columnMap, List<SortOrder> sortOrders) {
		return toPageable(pageLink, columnMap, sortOrders, true);
	}

	public static Pageable toPageable(PageLink pageLink, Map<String, String> columnMap, List<SortOrder> sortOrders, boolean addDefaultSorting) {
		return PageRequest.of(pageLink.getPageNumber(), pageLink.getPageSize(), pageLink.toSort(sortOrders, columnMap, addDefaultSorting));
	}

	public static <T> PageData<T> pageToPageData(Page<T> page) {
		return new PageData<>(page.getContent(), page.getTotalPages(), page.getTotalElements(), page.hasNext());
	}

	public static <T> T getData(ToData<T> data) {
		T object = null;
		if (data != null) {
			object = data.toData();
		}
		return object;
	}

	public static <T> T getData(Optional<? extends ToData<T>> data) {
		T object = null;
		if (data.isPresent()) {
			object = data.get().toData();
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
}
