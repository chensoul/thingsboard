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
package org.thingsboard.data.model.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.thingsboard.common.exception.ThingsboardErrorCode;
import org.thingsboard.common.exception.ThingsboardException;

@Data
public class PageLink {

	protected static final String DEFAULT_SORT_PROPERTY = "id";
	private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, DEFAULT_SORT_PROPERTY);

	private int pageSize;
	private int pageNumber;
	private String textSearch = "";
	private String sortProperty;
	private String sortOrder;

	public PageLink() {
		checkPageNumber();
		checkPageSize();
	}

	public PageLink(int pageSize, int pageNumber, String textSearch, String sortProperty, String sortOrder) {
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.textSearch = textSearch;
		this.sortProperty = sortProperty;
		this.sortOrder = sortOrder;

		checkPageNumber();
		checkPageSize();
	}

	public void checkPageNumber() {
		if (this.pageNumber < 0) {
			this.pageNumber = 0;
		}
	}

	public void checkPageSize() {
		if (this.pageSize < 1) {
			this.pageSize = 10;
		}
		if (this.pageSize > 1000) {
			this.pageSize = 1000;
		}
	}

	public PageLink(int pageSize) {
		this(pageSize, 0);
	}

	public PageLink(int pageSize, int pageNumber) {
		this(pageSize, pageNumber, null, null, null);
	}

	public PageLink(int pageSize, int pageNumber, String textSearch) {
		this(pageSize, pageNumber, textSearch, null, null);
	}

	public PageLink(int pageSize, int pageNumber, String textSearch, String sortProperty) {
		this(pageSize, pageNumber, textSearch, sortProperty, null);
	}

	public PageLink(PageLink pageLink) {
		this.pageSize = pageLink.getPageSize();
		this.pageNumber = pageLink.getPageNumber();
		this.textSearch = pageLink.getTextSearch();
		this.sortProperty = pageLink.getSortProperty();
		this.sortOrder = pageLink.getSortOrder();

		checkPageNumber();
		checkPageSize();
	}

	@JsonIgnore
	public PageLink nextPageLink() {
		return new PageLink(this.pageSize, this.pageNumber + 1, this.textSearch, this.sortProperty, this.sortOrder);
	}

	public SortOrder getSort() {
		if (StringUtils.isBlank(sortProperty)) {
			return null;
		}

		SortOrder.Direction direction = SortOrder.Direction.ASC;
		if (StringUtils.isNotEmpty(sortOrder)) {
			try {
				direction = SortOrder.Direction.valueOf(sortOrder.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ThingsboardException("Unsupported sort order '" + sortOrder + "'! Only 'ASC' or 'DESC' types are allowed.", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
			}
		}
		return new SortOrder(sortProperty, direction);
	}

	public Sort toSort(SortOrder order, Map<String, String> columnMap, boolean addDefaultSorting) {
		if (order == null) {
			return DEFAULT_SORT;
		} else {
			return toSort(List.of(order), columnMap, addDefaultSorting);
		}
	}

	public Sort toSort(List<SortOrder> orders, Map<String, String> columnMap, boolean addDefaultSorting) {
		if (addDefaultSorting && !isDefaultSortOrderAvailable(orders)) {
			orders = new ArrayList<>(orders);
			orders.add(new SortOrder(DEFAULT_SORT_PROPERTY, SortOrder.Direction.ASC));
		}
		return Sort.by(orders.stream().map(s -> toSortOrder(s, columnMap)).collect(Collectors.toList()));
	}

	private Sort.Order toSortOrder(SortOrder sortOrder, Map<String, String> columnMap) {
		String property = sortOrder.getProperty();
		if (columnMap.containsKey(property)) {
			property = columnMap.get(property);
		}
		return new Sort.Order(Sort.Direction.fromString(sortOrder.getDirection().name()), property);
	}

	public boolean isDefaultSortOrderAvailable(List<SortOrder> sortOrders) {
		for (SortOrder sortOrder : sortOrders) {
			if (DEFAULT_SORT_PROPERTY.equals(sortOrder.getProperty())) {
				return true;
			}
		}
		return false;
	}

}
