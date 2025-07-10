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
package org.thingsboard.domain.iot;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.model.ExtraBaseData;
import org.thingsboard.common.model.HasImage;
import org.thingsboard.common.model.HasName;
import org.thingsboard.common.validation.Length;
import org.thingsboard.common.validation.NoXss;


@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Project extends ExtraBaseData<Long> implements HasImage, HasName {
    @NoXss
    @Length
    private String name;

    private String image;

    private String description;

    private int status;

}
