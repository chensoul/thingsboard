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
package org.thingsboard.domain.iot.event;

import lombok.Getter;

public enum EventType {
	ERROR("error_event", "ERROR"),
	LC_EVENT("lc_event", "LC_EVENT"),
	STATS("stats_event", "STATS");

	@Getter
	private final String table;
	@Getter
	private final String oldName;
	@Getter
	private final boolean debug;

	EventType(String table, String oldName) {
		this(table, oldName, false);
	}

	EventType(String table, String oldName, boolean debug) {
		this.table = table;
		this.oldName = oldName;
		this.debug = debug;
	}

}
