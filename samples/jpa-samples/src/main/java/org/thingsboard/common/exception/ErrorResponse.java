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
package org.thingsboard.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString
public class ErrorResponse {
	private final HttpStatus status;

	private final String message;

	private final ThingsboardErrorCode code;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private final LocalDateTime timestamp;

	protected ErrorResponse(final String message, final ThingsboardErrorCode code, HttpStatus status) {
		this.message = message;
		this.code = code;
		this.status = status;
		this.timestamp = LocalDateTime.now();
	}

	public static ErrorResponse of(final String message, final ThingsboardErrorCode code, HttpStatus status) {
		return new ErrorResponse(message, code, status);
	}

	public Integer getStatus() {
		return status.value();
	}

	public String getMessage() {
		return message;
	}

	public ThingsboardErrorCode getCode() {
		return code;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
