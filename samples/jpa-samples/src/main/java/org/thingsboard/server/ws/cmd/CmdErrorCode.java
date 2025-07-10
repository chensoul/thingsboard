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
package org.thingsboard.server.ws.cmd;

import lombok.Getter;

@Getter
public enum CmdErrorCode {

	NO_ERROR(0),
	INTERNAL_ERROR(1, "Internal Server error!"),
	BAD_REQUEST(2, "Bad request"),
	UNAUTHORIZED(3, "Unauthorized");

	private final int code;
	private final String message;

	private CmdErrorCode(int code) {
		this(code, null);
	}

	private CmdErrorCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public static CmdErrorCode forCode(int code) {
		for (CmdErrorCode errorCode : CmdErrorCode.values()) {
			if (errorCode.getCode() == code) {
				return errorCode;
			}
		}
		throw new IllegalArgumentException("Invalid error code: " + code);
	}
}
