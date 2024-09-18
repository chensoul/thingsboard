package com.chensoul.system.infrastructure.security.rest;

import com.chensoul.spring.util.ServletUtils;
import com.chensoul.util.InetAddressUtils;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;

@Data
public class RestAuthenticationDetail implements Serializable {
    private final String serverAddress;
    private final String clientAddress;
    private final String userAgent;

    public RestAuthenticationDetail(HttpServletRequest request) {
        this.clientAddress = ServletUtils.getClientIp(request);
        this.serverAddress = InetAddressUtils.getLocalhostStr();
        this.userAgent = ServletUtils.getUserAgent(request);
    }
}
