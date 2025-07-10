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
package org.thingsboard.domain.iot.ota;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.model.ExtraBaseData;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.model.HasTenantId;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class OtaPackage extends ExtraBaseData<Long> implements HasName, HasTenantId {

    private static final long serialVersionUID = 3168391583570815419L;

    private String tenantId;
    private Long deviceProfileId;
    private OtaPackageType type;
    @Length
    @NoXss
    private String name;
    @Length
    @NoXss
    private String version;
    @Length
    @NoXss
    private String tag;
    @Length
    @NoXss
    private String url;
    private boolean hasData;
    @Length
    @NoXss
    private String fileName;
    @NoXss
    @Length
    private String contentType;
    private ChecksumAlgorithm checksumAlgorithm;
    @Length(max = 1020)
    @Schema(description = "OTA Package checksum.", example = "0xd87f7e0c", accessMode = Schema.AccessMode.READ_ONLY)
    private String checksum;
    @Schema(description = "OTA Package data size.", example = "8", accessMode = Schema.AccessMode.READ_ONLY)
    private Long dataSize;
}
