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
