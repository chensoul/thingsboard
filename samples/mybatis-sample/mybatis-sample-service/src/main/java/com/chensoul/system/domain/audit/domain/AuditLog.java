package com.chensoul.system.domain.audit.domain;

import com.chensoul.data.model.BaseDataWithExtra;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuditLog extends BaseDataWithExtra<Long> {
    private String serviceId;
    private String clientId;
    private String tenantId;
    private Long merchantId;
    private Long userId;
    private String userName;

    private String traceId;
    private String requestUri;
    private String userAgent;
    private String userIp;

    private String entityId;
    private String entityName;
    private EntityType entityType;
    private ActionType actionType;
    private ActionStatus actionStatus;
    private String failureDetail;
    private Long costTime;
}
