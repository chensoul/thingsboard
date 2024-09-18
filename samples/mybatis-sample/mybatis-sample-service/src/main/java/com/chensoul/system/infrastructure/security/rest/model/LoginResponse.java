package com.chensoul.system.infrastructure.security.rest.model;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private String refreshToken;

}
