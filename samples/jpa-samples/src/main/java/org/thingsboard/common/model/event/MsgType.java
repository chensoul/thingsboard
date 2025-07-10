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
package org.thingsboard.common.model.event;

import lombok.Getter;

public enum MsgType {
	ENTITY_CREATED("Entity Created"),
	ENTITY_UPDATED("Entity Updated"),
	ENTITY_DELETED("Entity Deleted"),
	SEND_EMAIL,
	NA;

	@Getter
	private final String ruleNodeConnection;

	@Getter
	private final boolean tellSelfOnly;

	MsgType(String ruleNodeConnection, boolean tellSelfOnly) {
		this.ruleNodeConnection = ruleNodeConnection;
		this.tellSelfOnly = tellSelfOnly;
	}

	MsgType(String ruleNodeConnection) {
		this(ruleNodeConnection, false);
	}

	MsgType() {
		this(null, false);
	}

}
