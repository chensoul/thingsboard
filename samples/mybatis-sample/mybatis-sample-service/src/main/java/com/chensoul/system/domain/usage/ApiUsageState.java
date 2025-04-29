/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
