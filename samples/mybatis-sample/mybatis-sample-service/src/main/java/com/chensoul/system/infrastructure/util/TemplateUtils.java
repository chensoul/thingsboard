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
package com.chensoul.system.infrastructure.util;

import com.chensoul.util.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class TemplateUtils {
    private static final Pattern TEMPLATE_PARAM_PATTERN = Pattern.compile("\\$\\{(.+?)(:[a-zA-Z]+)?}");

    private static final Map<String, UnaryOperator<String>> FUNCTIONS = Maps.of(
        "upperCase", String::toUpperCase,
        "lowerCase", String::toLowerCase,
        "capitalize", StringUtils::capitalize
    );

    private TemplateUtils() {
    }

    public static String processTemplate(String template, Map<String, String> context) {
        Matcher matcher = TEMPLATE_PARAM_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = Optional.ofNullable(context.get(key)).orElse("");
            String function = matcher.group(2); // 函数名称，可能为null

            // 应用函数，如果指定了函数名称
            if (function != null && FUNCTIONS.containsKey(function)) {
                value = FUNCTIONS.get(function).apply(value);
            }
            // 替换匹配项
            matcher.appendReplacement(sb, value);
        }
        matcher.appendTail(sb);

        // 返回处理后的字符串
        return sb.toString();
    }

}
