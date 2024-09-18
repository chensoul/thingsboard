package com.chensoul.system.domain.usage;

import com.chensoul.data.model.BaseData;
import com.chensoul.data.model.HasTenantId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class ApiUsageState extends BaseData<Long> implements HasTenantId {

    private static final long serialVersionUID = 8250339805336035966L;

    private String tenantId;
    private String entityId;
    private ApiUsageStateValue transportState;
    private ApiUsageStateValue dbStorageState;
    private ApiUsageStateValue reExecState;
    private ApiUsageStateValue jsExecState;
    private ApiUsageStateValue tbelExecState;
    private ApiUsageStateValue emailExecState;
    private ApiUsageStateValue smsExecState;
    private ApiUsageStateValue alarmExecState;

    public boolean isTransportEnabled() {
        return !ApiUsageStateValue.DISABLED.equals(transportState);
    }

    public boolean isReExecEnabled() {
        return !ApiUsageStateValue.DISABLED.equals(reExecState);
    }

    public boolean isDbStorageEnabled() {
        return !ApiUsageStateValue.DISABLED.equals(dbStorageState);
    }

    public boolean isJsExecEnabled() {
        return !ApiUsageStateValue.DISABLED.equals(jsExecState);
    }

    public boolean isTbelExecEnabled() {
        return !ApiUsageStateValue.DISABLED.equals(tbelExecState);
    }

    public boolean isEmailSendEnabled() {
        return !ApiUsageStateValue.DISABLED.equals(emailExecState);
    }

    public boolean isSmsSendEnabled() {
        return !ApiUsageStateValue.DISABLED.equals(smsExecState);
    }

    public boolean isAlarmCreationEnabled() {
        return alarmExecState != ApiUsageStateValue.DISABLED;
    }
}
