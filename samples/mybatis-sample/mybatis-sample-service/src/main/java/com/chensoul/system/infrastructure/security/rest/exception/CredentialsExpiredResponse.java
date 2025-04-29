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
package com.chensoul.system.infrastructure.security.rest.exception;

import com.chensoul.exception.ResultCode;
import com.chensoul.util.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class CredentialsExpiredResponse extends ErrorResponse {

    private final String resetToken;

    protected CredentialsExpiredResponse(String message, String resetToken) {
        super(ResultCode.CREDENTIALS_EXPIRED.getCode(), message);
        this.resetToken = resetToken;
    }

    public static CredentialsExpiredResponse of(final String message, final String resetToken) {
        return new CredentialsExpiredResponse(message, resetToken);
    }

    public String getResetToken() {
        return resetToken;
    }
}
