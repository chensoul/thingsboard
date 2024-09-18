package com.chensoul.system.infrastructure.security.jwt.extractor;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component(value = "jwtQueryTokenExtractor")
public class JwtQueryTokenExtractor implements TokenExtractor {

    @Override
    public String extract(HttpServletRequest request) {
        String token = null;
        if (request.getParameterMap() != null && !request.getParameterMap().isEmpty()) {
            String[] tokenParamValue = request.getParameterMap().get(JWT_TOKEN_QUERY_PARAM);
            if (tokenParamValue != null && tokenParamValue.length == 1) {
                token = tokenParamValue[0];
            }
        }

        if (StringUtils.isBlank(token)) {
            token = request.getHeader(JWT_TOKEN_QUERY_PARAM);
        }

        if (StringUtils.isBlank(token)) {
            throw new AuthenticationServiceException("Authorization query parameter cannot be blank!");
        }

        return token;
    }
}
