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
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TimePageLink extends PageLink {
	private final Long startTime;
	private final Long endTime;

	public TimePageLink(PageLink pageLink, Long startTime, Long endTime) {
		super(pageLink);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public TimePageLink(int pageSize) {
		this(pageSize, 0);
	}

	public TimePageLink(int pageSize, int pageNumber) {
		this(pageSize, pageNumber, null);
	}

	public TimePageLink(int pageSize, int pageNumber, String textSearch) {
		this(pageSize, pageNumber, textSearch, null, null, null, null);
	}

	public TimePageLink(int pageSize, int pageNumber, String textSearch, String sortProperty, String sortOrder) {
		this(pageSize, pageNumber, textSearch, sortProperty, sortOrder, null, null);
	}

	public TimePageLink(int pageSize, int pageNumber, String textSearch, String sortProperty) {
		this(pageSize, pageNumber, textSearch, sortProperty, null, null, null);
	}

	public TimePageLink(int pageSize, int pageNumber, String textSearch, String sortProperty, String sortOrder, Long startTime, Long endTime) {
		super(pageSize, pageNumber, textSearch, sortProperty, sortOrder);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@JsonIgnore
	public TimePageLink nextPageLink() {
		return new TimePageLink(this.getPageSize(), this.getPageNumber() + 1, this.getTextSearch(), this.getSortProperty(), this.getSortOrder(),
			this.startTime, this.endTime);
	}

}
