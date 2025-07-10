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
package org.thingsboard.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;
import org.thingsboard.common.validation.Create;

@Data
public abstract class BaseData<I> implements Serializable, HasId<I> {
    public static final ObjectMapper mapper = new ObjectMapper();
    private static final long serialVersionUID = 5422817607129962637L;
    @NotNull(message = "Id不能为空", groups = Create.class)
    protected I id;
    protected Long createdTime;
    protected Long updatedTime;

}
