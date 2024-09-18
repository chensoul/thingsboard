package com.chensoul.system.infrastructure.security.jwt.extractor;

import javax.servlet.http.HttpServletRequest;

public interface TokenExtractor {
    String JWT_TOKEN_HEADER_PARAM = "Authorization";
    String JWT_TOKEN_QUERY_PARAM = "token";

    String extract(HttpServletRequest request);
}
