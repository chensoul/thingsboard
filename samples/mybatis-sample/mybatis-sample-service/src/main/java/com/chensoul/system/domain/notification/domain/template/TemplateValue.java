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
package com.chensoul.system.domain.notification.domain.template;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class TemplateValue {
    private final Supplier<String> getter;
    private final Consumer<String> setter;

    public static TemplateValue of(Supplier<String> getter, Consumer<String> setter) {
        return new TemplateValue(getter, setter);
    }

    public String get() {
        return getter.get();
    }

    public void set(String processed) {
        setter.accept(processed);
    }

    public boolean containsParams(Collection<String> params) {
        return StringUtils.containsAny(get(), params.toArray(new String[]{}));
    }

}
