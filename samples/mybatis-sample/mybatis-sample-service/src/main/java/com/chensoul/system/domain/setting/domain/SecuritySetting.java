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
package com.chensoul.system.domain.setting.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SecuritySetting implements Serializable {
    private static final long serialVersionUID = -1307613974597312465L;
    private PasswordPolicy passwordPolicy = new PasswordPolicy();
    private Integer maxFailedLoginAttempts = 5;
    private String userLockoutNotificationEmail;

    @Data
    public static class PasswordPolicy implements Serializable {
        private Integer minimumLength = 6;
        private Integer maximumLength = 20;
        private Integer minimumUppercaseLetters;
        private Integer minimumLowercaseLetters;
        private Integer minimumDigits;
        private Integer minimumSpecialCharacters;
        private Boolean allowWhitespaces = true;
        private Boolean forceUserToResetPasswordIfNotValid = false;
        private Integer passwordExpirationPeriodDays;
        private Integer passwordReuseFrequencyDays;
    }
}
