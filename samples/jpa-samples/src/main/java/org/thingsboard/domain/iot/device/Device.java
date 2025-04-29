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
package org.thingsboard.domain.iot.device;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.model.ExtraBaseData;
import org.thingsboard.common.model.HasLabel;
import org.thingsboard.common.model.HasMerchantId;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;
import org.thingsboard.domain.iot.ota.HasOtaPackage;


@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Device extends ExtraBaseData<String> implements HasLabel, HasName, HasTenantId, HasMerchantId, HasOtaPackage {
    private static final long serialVersionUID = 2807343040519543363L;

    private String tenantId;

    private Long merchantId;

    @NoXss
    @Length
    private String name;

    @NoXss
    @Length
    private String description;

    @NoXss
    @Length
    private String type;

    @NoXss
    @Length
    private String label;

    private JsonNode deviceData;

    private Long deviceProfileId;

    private String firmwareId;

    private String softwareId;
}
