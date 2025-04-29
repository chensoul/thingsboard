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

import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ContactBased<I extends Serializable> extends ExtraBaseData<I> implements HasEmail {
    private static final long serialVersionUID = 5047448057830660988L;

    protected String phone;

    @Pattern(regexp = EMAIL_REGEXP, message = "Email address is not valid")
    protected String email;

    protected String country;

    protected String state;

    protected String city;

    protected String address;

    protected String address2;

    protected String zip;
}
