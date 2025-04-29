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
package org.thingsboard.common.util;

import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * The utility class of text format
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 0.0.1
 *
 */
public abstract class FormatUtils {
    public static final String DEFAULT_PLACEHOLDER = "{}";

    /**
     * <p>formatVariables.</p>
     *
     * @param template a {@link String} object
     * @param map a {@link Map} object
     * @return a {@link String} object
     */
    public static String formatVariables(final String template, final Map<String, ?> map) {
        return formatVariables(template, "{", "}", map);
    }

    /**
     * <p>formatVariables.</p>
     *
     * @param template a {@link String} object
     * @param prefix a {@link String} object
     * @param suffix a {@link String} object
     * @param map a {@link Map} object
     * @return a {@link String} object
     */
    public static String formatVariables(final String template, String prefix, String suffix, final Map<String, ?> map) {
        if (null == template) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return template;
        }

        String template2 = template.toString();
        String value;
        for (final Map.Entry<String, ?> entry : map.entrySet()) {
			value = Objects.toString(entry.getValue(), EMPTY);
			if (entry.getValue().equals("null")) {
				value = EMPTY;
            }
            if (!StringUtils.isEmpty(value)) {
                template2 = StringUtils.replace(template2, prefix + entry.getKey() + suffix, value);
            }
        }
        return template2;
    }

    /**
     * <p>format.</p>
     *
     * @param template a {@link String} object
     * @param args a {@link Object} object
     * @return a {@link String} object
     */
    public static String format(final String template, final Object... args) {
        return formatWithPlaceholder(template, DEFAULT_PLACEHOLDER, args);
    }

    /**
     * <p>formatWithPlaceholder.</p>
     *
     * @param template a {@link String} object
     * @param placeholder a {@link String} object
     * @param args a {@link Object} object
     * @return a {@link String} object
     */
    public static String formatWithPlaceholder(final String template, final String placeholder, final Object... args) {
        int argsLength = args == null ? 0 : args.length;
        if (argsLength == 0) {
            return template;
        }

        StringBuilder stringBuilder = new StringBuilder(template);
        int index = -1;
        for (int i = 0; i < argsLength; i++) {
            index = stringBuilder.indexOf(placeholder);
            if (index == -1) {
                break;
            }
            String value = String.valueOf(args[i]);
            stringBuilder.replace(index, index + placeholder.length(), value);
        }
        return stringBuilder.toString();
    }
}
