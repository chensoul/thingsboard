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
package org.thingsboard.domain.audit;

import java.util.Optional;
import lombok.Getter;
import org.thingsboard.common.model.event.MsgType;

public enum ActionType {

	ADD(false, MsgType.ENTITY_CREATED), // log entity
	DELETE(false, MsgType.ENTITY_DELETED), // log string id
	UPDATE(false, MsgType.ENTITY_UPDATED),
	CREDENTIALS_UPDATE(false, null),
	LOGIN(false, null),
	LOGOUT(false, null),
	LOCKOUT(false, null),
	SMS_SENT(false, null);

	@Getter
	private final boolean isRead;

	private final MsgType ruleEngineMsgType;

	ActionType(boolean isRead, MsgType ruleEngineMsgType) {
		this.isRead = isRead;
		this.ruleEngineMsgType = ruleEngineMsgType;
	}

	public Optional<MsgType> getRuleEngineMsgType() {
		return Optional.ofNullable(ruleEngineMsgType);
	}

}
