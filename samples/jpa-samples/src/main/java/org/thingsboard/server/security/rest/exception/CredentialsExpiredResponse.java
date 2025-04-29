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
package org.thingsboard.server.security.rest.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.thingsboard.common.exception.ErrorResponse;
import org.thingsboard.common.exception.ThingsboardErrorCode;

@Schema
public class CredentialsExpiredResponse extends ErrorResponse {

    private final String resetToken;

    protected CredentialsExpiredResponse(String message, String resetToken) {
        super(message, ThingsboardErrorCode.CREDENTIALS_EXPIRED, HttpStatus.UNAUTHORIZED);
        this.resetToken = resetToken;
    }

    public static CredentialsExpiredResponse of(final String message, final String resetToken) {
        return new CredentialsExpiredResponse(message, resetToken);
    }

    public String getResetToken() {
        return resetToken;
    }
}
