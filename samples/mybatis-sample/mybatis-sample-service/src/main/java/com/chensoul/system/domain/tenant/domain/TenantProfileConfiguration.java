package com.chensoul.system.domain.tenant.domain;

import com.chensoul.system.domain.usage.ApiUsageRecordKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DefaultTenantProfileConfiguration.class, name = "DEFAULT")})
public interface TenantProfileConfiguration extends Serializable {

    @JsonIgnore
    TenantProfileType getType();

    @JsonIgnore
    long getProfileThreshold(ApiUsageRecordKey key);

    @JsonIgnore
    boolean getProfileFeatureEnabled(ApiUsageRecordKey key);

    @JsonIgnore
    long getWarnThreshold(ApiUsageRecordKey key);

    @JsonIgnore
    int getMaxRuleNodeExecsPerMessage();

}
