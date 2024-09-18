package com.chensoul.system.infrastructure.security.jwt.token;

import com.chensoul.system.user.domain.Authority;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtPair implements Serializable {

    private String token;
    private String refreshToken;
    private Authority scope;
}
