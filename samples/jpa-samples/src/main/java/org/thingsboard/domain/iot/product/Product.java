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
package org.thingsboard.domain.iot.product;

import lombok.Data;
import org.thingsboard.common.model.ExtraBaseData;
import org.thingsboard.common.model.HasImage;
import org.thingsboard.common.model.HasName;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
public class Product extends ExtraBaseData<String> implements HasImage, HasName {
    private String name;

    private String image;

    private String description;

    private String thingModelId;

    private int status;

    private Boolean isPublic;

    private AccessType accessType;

    private ProductNetType netType;

    //	@ApiModelProperty(value = "跳过生产流程")
    private Boolean hasProduction;
}
