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
package org.thingsboard.server.ws.cmd;

import lombok.Getter;

public class DataUpdate<T> extends CmdUpdate {

	@Getter
	private final T data;

	public DataUpdate(int cmdId, T data, int code, String message) {
		super(cmdId, code, message);
		this.data = data;
	}

	public DataUpdate(int cmdId, T data) {
		this(cmdId, data, CmdErrorCode.NO_ERROR.getCode(), null);
	}

	public DataUpdate(int cmdId, int code, String message) {
		this(cmdId, null, code, message);
	}

}
