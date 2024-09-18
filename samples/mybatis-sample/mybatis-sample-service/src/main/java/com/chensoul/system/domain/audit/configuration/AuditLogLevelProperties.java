package com.chensoul.system.domain.audit.configuration;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "audit-log.logging-level")
public class AuditLogLevelProperties {

    private Map<String, String> mask = new HashMap<>();

    public AuditLogLevelProperties() {
        super();
    }

    public Map<String, String> getMask() {
        return this.mask;
    }

    public void setMask(Map<String, String> mask) {
        this.mask = mask;
    }
}
